package com.example.messagingapp.http

import com.example.messagingapp.Environment
import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.repository.jdbi.JdbiTransactionManager
import com.example.messagingapp.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChannelsTests {
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `all channel things`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        val username = generateRandomString()
        val otherUsername = generateRandomString()
        val userId = bootstrapUser(username)
        val otherUser = bootstrapUser(otherUsername)

        val token =
            transactionManager.run {
                val now = Clock.System.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        val otherUserToken =
            transactionManager.run {
                val now = Clock.System.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), otherUser, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // CREATE CHANNEL
        client.post().uri("/channel")
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "channelName" to "channelName",
                    "isPublic" to true,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // GET CHANNEL BY ID
        client.get().uri("/channel/1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        // GET CHANNEL BY ID WRONG USER
        client.get().uri("/channel/1")
            .header("Authorization", "Bearer $otherUserToken")
            .exchange()
            .expectStatus().isForbidden
            .expectBody()

        bootstrapChannel(userId)
        bootstrapChannel(otherUser)
        bootstrapChannel(otherUser, false)

        // GET JOINED CHANNELS
        val channels =
            client.get().uri("/channel")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .returnResult()
                .responseBody

        // SEARCH CHANNELS
        client.get().uri("/channel/search")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ChannelWithMembershipOutputModel::class.java)
            .returnResult()
            .responseBody

        // MUST FIND 3 CHANNELS SINCE ONE CREATED IS PRIVATE

        // INVITE
        client.post().uri("/channel/1/memberships")
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to otherUsername,
                    "role" to "member",
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // SEND MESSAGE
        client.post().uri("/channel/1")
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // GET MESSAGES
        val message =
            client.get().uri("/channel/1")
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isOk
        // .expectBodyList(MessageOutput::class.java)

        println("message -> \n --------------------------------------------")

        println(message)
        // MUST FIND THIS 1 MESSAGE

        // GET INVITATIONS
        val invitation =
            client.get().uri("/channel/invitations")
                .header("Authorization", "Bearer $otherUserToken")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(ChannelInvitationOutputModel::class.java)
                .returnResult()
                .responseBody

        val inviteId = invitation?.get(0)?.channelInvitationId

        client.post().uri("/channel/invitations")
            .header("Authorization", "Bearer $otherUserToken")
            .bodyValue(
                mapOf(
                    "response" to true,
                    "inviteId" to inviteId,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // LEAVE CHANNEL
        client.delete().uri("/channel/1/memberships")
            .header("Authorization", "Bearer $otherUserToken")
            .exchange()
            .expectStatus().isNoContent
    }

    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val transactionManager = JdbiTransactionManager(jdbi)

        private val userDomain =
            UserDomain(
                BCryptPasswordEncoder(),
                UUIDTokenEncoder(),
                UserDomainConfig(30.days, 30.minutes, 3, 24.hours),
            )

        private fun bootstrapUser(
            username: String = generateRandomString(),
            password: String = "Password123@",
        ): Int {
            return transactionManager.run {
                return@run it.usersRepository.createUser(
                    username,
                    generateRandomEmail(),
                    userDomain.hashPassword("Password123@"),
                )
            }
        }

        private fun bootstrapChannel(
            userId: Int,
            isPublic: Boolean = true,
            name: String = generateRandomString(),
        ): Int {
            return transactionManager.run {
                return@run it.channelsRepository.createChannel(name, isPublic, userId)
            }
        }
    }
}
