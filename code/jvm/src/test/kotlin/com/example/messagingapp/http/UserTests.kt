package com.example.messagingapp.http

import com.example.messagingapp.Environment
import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomString
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
class UserTests {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `User can be created`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: an invitation
        val inviterId = bootstrapUser()
        val inviteeEmail = generateRandomEmail()
        val invitationToken =
            transactionManager.run {
                it.usersRepository.createRegistrationInvitation(inviterId, Clock.System.now())
            }
        val inviteeUsername = generateRandomString()
        val inviteePassword = "Password123@"

        // when: creating an user
        // then: the response is a 201
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "password" to inviteePassword,
                    "invitationToken" to invitationToken.value,
                    "email" to inviteeEmail,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: creating a user with the same invitation token
        // then: the response is a 400
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to generateRandomString(),
                    "password" to "Password123@",
                    "invitationToken" to invitationToken.value,
                    "email" to generateRandomEmail(),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an invalid invitation token
        // then: the response is a 400

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to generateRandomString(),
                    "password" to "Password123@",
                    "invitationToken" to "invalid",
                    "email" to generateRandomEmail(),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an invalid email
        // then: the response is a 400

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to generateRandomString(),
                    "password" to "Password123@",
                    "email" to "invalid",
                    "invitationToken" to invitationToken.value,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an invalid password
        // then: the response is a 400

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to generateRandomString(),
                    "password" to "invalid",
                    "email" to generateRandomEmail(),
                    "invitationToken" to invitationToken.value,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an invalid username
        // then: the response is a 400

        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to "invalid",
                    "password" to "Password123@",
                    "email" to generateRandomEmail(),
                    "invitationToken" to invitationToken.value,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `User can login`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a user
        val userId = bootstrapUser()
        val username =
            transactionManager.run {
                it.usersRepository.getUserById(userId)?.username ?: throw AssertionError("User not found")
            }

        // when: logging in
        // then: the response is a 200

        client.post().uri("/login")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to "Password123@",
                ),
            )
            .exchange()
            .expectStatus().isOk

        // when: logging in with an invalid username
        // then: the response is a 400

        client.post().uri("/login")
            .bodyValue(
                mapOf(
                    "username" to "invalid",
                    "password" to "Password123@",
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: logging in with an invalid password
        // then: the response is a 400

        client.post().uri("/login")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to "invalid",
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `User can logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a user
        val userId = bootstrapUser()

        val token =
            transactionManager.run {
                val now = Clock.System.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: logging out
        // then: the response is a 204

        client.post().uri("/logout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `User can be invited`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a user
        val userId = bootstrapUser()

        val token =
            transactionManager.run {
                val now = Clock.System.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: inviting a user
        // then: the response is a 201

        client.post().uri("/invite")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isCreated

        // when: inviting a user when not logged in
        // then: the response is a 401
        client.post().uri("/invite")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `User can access own profile page`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a user
        val userId = bootstrapUser()

        val token =
            transactionManager.run {
                val now = Clock.System.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: accessing the profile page
        // then: the response is a 200

        client.get().uri("/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // when: accessing the profile page when not logged in
        // then: the response is a 401
        client.get().uri("/me")
            .exchange()
            .expectStatus().isUnauthorized
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

        private fun bootstrapUser(): Int {
            return transactionManager.run {
                return@run it.usersRepository.createUser(
                    generateRandomString(),
                    generateRandomEmail(),
                    userDomain.hashPassword("Password123@"),
                )
            }
        }
    }
}
