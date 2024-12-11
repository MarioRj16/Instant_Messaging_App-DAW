package com.example.messagingapp.http

import com.example.messagingapp.TestClock
import com.example.messagingapp.bootstrapChannel
import com.example.messagingapp.bootstrapUser
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.generateToken
import com.example.messagingapp.jdbi
import com.example.messagingapp.transactionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChannelTests {
    @AfterEach
    fun tearDown() {
        clearDatabase(jdbi)
    }

    @LocalServerPort
    var port: Int = 0

    val baseURL: String
        get() = "http://localhost:$port"

    @Test
    fun `Channel can be created`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        // when: creating a channel logged in
        // then: the response is a 201
        client.post().uri(Uris.Channels.BASE)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "channelName" to "channelName",
                    "isPublic" to false,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: creating a channel without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.BASE)
            .bodyValue(
                mapOf(
                    "channelName" to "channelName2",
                    "isPublic" to false,
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: creating a channel with existing channel name
        // then: the response is a 400
        client.post().uri(Uris.Channels.BASE)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "channelName" to "channelName",
                    "isPublic" to false,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a channel with invalid channel name
        // then: the response is a 400
        client.post().uri(Uris.Channels.BASE)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "channelName" to "a",
                    "isPublic" to false,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `Channel can be retrieved`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        val channelId = bootstrapChannel(userId)

        // when: getting a channel by id
        // then: the response is a 200
        client.get().uri(Uris.Channels.GET_BY_ID, channelId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // when: getting a channel by id without being logged in
        // then: the response is a 401
        client.get().uri(Uris.Channels.GET_BY_ID, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: getting a channel by id that does not exist
        // then: the response is a 404
        client.get().uri(Uris.Channels.GET_BY_ID, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `Joined channels can be retrieved`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        // when: getting joined channels without joining any
        // then: the response is a 200
        client.get().uri(Uris.Channels.BASE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        bootstrapChannel(userId)

        // when: getting joined channels after joining one
        // then: the response is a 200
        client.get().uri(Uris.Channels.BASE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        // when: getting joined channels without being logged in
        // then: the response is a 401
        client.get().uri(Uris.Channels.BASE)
            .exchange()
            .expectStatus().isUnauthorized

        // when getting joined channels with invalid page
        // then: the response is a 400
        client.get().uri(Uris.Channels.BASE + "?page=-1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest

        // when getting joined channels with invalid page size
        // then: the response is a 400
        client.get().uri(Uris.Channels.BASE + "?pageSize=0")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `Channel can be joined`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val ownerId = bootstrapUser(testClock = clock)
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        val channelId = bootstrapChannel(ownerId = ownerId, isPublic = true)

        // when: joining a channel
        // then: the response is a 201
        client.post().uri(Uris.Channels.JOIN, channelId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        val membership =
            transactionManager.run {
                it.channelsRepository.getMembership(channelId, userId)
            }
        assertNotNull(membership)

        // when: joining a channel without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.JOIN, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: joining a channel user is already a member of
        // then: the response is a 409
        client.post().uri(Uris.Channels.JOIN, channelId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)

        // when: joining a channel that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.JOIN, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound

        // when: joining a channel that is not public
        // then: the response is a 403
        val otherUserId = bootstrapUser(testClock = clock)

        val otherToken = generateToken(otherUserId, clock)

        val otherChannelId = bootstrapChannel(otherUserId, isPublic = false)

        client.post().uri(Uris.Channels.JOIN, otherChannelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `Channel can be searched`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        // when: searching for channels without any
        // then: the response is a 200
        client.get().uri(Uris.Channels.SEARCH)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        val channelName = "channelName"
        bootstrapChannel(userId, channelName = channelName)

        // when: searching for channels after creating one
        // then: the response is a 200
        client.get().uri(Uris.Channels.SEARCH)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody() // TODO: Confirm that it contains the channel

        // when: searching for channels without being logged in
        // then: the response is a 401
        client.get().uri(Uris.Channels.SEARCH)
            .exchange()
            .expectStatus().isUnauthorized

        // when searching for channels with invalid page
        // then: the response is a 400
        client.get().uri(Uris.Channels.SEARCH + "?page=-1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest

        // when searching for channels with invalid page size
        // then: the response is a 400
        client.get().uri(Uris.Channels.SEARCH + "?pageSize=0")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `Messages can be listed`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)
        val otherUserId = bootstrapUser(testClock = clock)
        val channelId = bootstrapChannel(userId)

        val token = generateToken(userId, clock)
        val otherToken = generateToken(otherUserId, clock)

        // when: listing messages without any
        // then: the response is a 200
        client.get().uri(Uris.Channels.MESSAGES, channelId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        // when: listing messages without being logged in
        // then: the response is a 401

        client.get().uri(Uris.Channels.MESSAGES, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: listing messages from a channel that does not exist
        // then: the response is a 404
        client.get().uri(Uris.Channels.MESSAGES, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound

        // when: listing messages from a channel that user is not a member of
        // then: the response is a 403
        client.get().uri(Uris.Channels.MESSAGES, channelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `Message can be sent`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)
        val otherUsedId = bootstrapUser(testClock = clock)
        val channelId = bootstrapChannel(userId)

        val token = generateToken(userId, clock)
        val otherToken = generateToken(otherUsedId, clock)

        // when: sending a message
        // then: the response is a 201
        client.post().uri(Uris.Channels.MESSAGES, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: sending a message without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.MESSAGES, channelId)
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: sending a message to a channel that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.MESSAGES, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
            .exchange()
            .expectStatus().isNotFound

        // when: sending a message to a channel that user is not a member of
        // then: the response is a 403
        client.post().uri(Uris.Channels.MESSAGES, channelId)
            .header("Authorization", "Bearer $otherToken")
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
            .exchange()
            .expectStatus().isForbidden

        // when: sending a message with no permission
        // then: the response is a 403
        val userId2 = bootstrapUser(testClock = clock)
        transactionManager.run {
            it.channelsRepository.createMembership(userId2, channelId, clock, MembershipRole.VIEWER.role)
        }

        client.post().uri(Uris.Channels.MESSAGES, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "content" to "message123",
                ),
            )
    }

    @Test
    fun `Invitations can be listed`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)

        val token = generateToken(userId, clock)

        // when: listing invitations without any
        // then: the response is a 200
        client.get().uri(Uris.Channels.INVITATIONS)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        // when: listing invitations without being logged in
        // then: the response is a 401
        client.get().uri(Uris.Channels.INVITATIONS)
            .exchange()
            .expectStatus().isUnauthorized

        // when listing invitations with invalid page
        // then: the response is a 400
        client.get().uri(Uris.Channels.INVITATIONS + "?page=-1")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest

        // when listing invitations with invalid page size
        // then: the response is a 400
        client.get().uri(Uris.Channels.INVITATIONS + "?pageSize=0")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `Invitation can be accepted`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)
        val inviteeUsername = "username"
        val otherUserId = bootstrapUser(username = inviteeUsername, testClock = clock)
        val channelId = bootstrapChannel(userId)

        val token = generateToken(userId, clock)
        val otherToken = generateToken(otherUserId, clock)

        // when: inviting a user
        // then: the response is a 201
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: accepting an invitation
        // then: the response is a 201
        client.post().uri(Uris.Channels.ACCEPT_INVITATION, channelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isCreated

        // when: accepting an invitation without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.ACCEPT_INVITATION, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: accepting an invitation that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.ACCEPT_INVITATION, Int.MAX_VALUE)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `Invitation can be declined`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)
        val inviteeUsername = "username"
        val otherUserId = bootstrapUser(username = inviteeUsername, testClock = clock)
        val channelId = bootstrapChannel(userId)

        val token = generateToken(userId, clock)
        val otherToken = generateToken(otherUserId, clock)

        // when: inviting a user
        // then: the response is a 201
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: declining an invitation
        // then: the response is a 201
        client.post().uri(Uris.Channels.DECLINE_INVITATION, channelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isCreated

        // when: declining an invitation without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.DECLINE_INVITATION, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: declining an invitation that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.DECLINE_INVITATION, Int.MAX_VALUE)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `Channel invitations can be created`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val ownerUsername = "owner"
        val userId = bootstrapUser(username = ownerUsername, testClock = clock)
        val inviteeUsername = "username"
        val userId2 = bootstrapUser(username = inviteeUsername, testClock = clock)
        val inviteeUsername2 = "username2"
        bootstrapUser(username = inviteeUsername2, testClock = clock)
        val channelId = bootstrapChannel(userId)

        val token = generateToken(userId, clock)
        val token2 = generateToken(userId2, clock)

        // when: inviting a user
        // then: the response is a 201
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // cleanup
        deleteInvitation(channelId, userId2)

        // when: inviting a user without being logged in
        // then: the response is a 401
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: inviting a user to a channel that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.MEMBERS, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isNotFound

        // when: inviting a user to a channel that inviter is not a member of
        // then: the response is a 401
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token2")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername2,
                    "role" to MembershipRole.VIEWER.role,
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: inviting a user that does not exist
        // then: the response is a 404
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to generateRandomString(),
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isNotFound


        // when: inviting a user that is already a member
        // then: the response is a 409
        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to ownerUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)


        // when: inviting a user twice
        // then: the response is a 409
        transactionManager.run {
            it.channelsRepository.createChannelInvitation(
                channelId, userId, userId2, MembershipRole.VIEWER.role, clock
            )
        }

        client.post().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to inviteeUsername,
                    "role" to MembershipRole.MEMBER.role,
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `Channel can be left`() {
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()
        val userId = bootstrapUser(testClock = clock)
        val otherUserId = bootstrapUser(testClock = clock)
        val channelId = bootstrapChannel(userId)

        transactionManager.run {
            it.channelsRepository.createMembership(otherUserId, channelId, clock, MembershipRole.MEMBER.role)
        }

        val token = generateToken(userId, clock)
        val otherToken = generateToken(otherUserId, clock)

        // when: leaving a channel that user is the owner of
        // then: the response is a 403
        client.delete().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isForbidden

        // when: leaving a channel
        // then: the response is a 204
        client.delete().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isNoContent

        // when: leaving a channel without being logged in
        // then: the response is a 401
        client.delete().uri(Uris.Channels.MEMBERS, channelId)
            .exchange()
            .expectStatus().isUnauthorized

        // when: leaving a channel that does not exist
        // then: the response is a 404
        client.delete().uri(Uris.Channels.MEMBERS, Int.MAX_VALUE)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound

        // when: leaving a channel that user is not a member of
        // then: the response is a 403
        client.delete().uri(Uris.Channels.MEMBERS, channelId)
            .header("Authorization", "Bearer $otherToken")
            .exchange()
            .expectStatus().isForbidden
    }

    private fun deleteInvitation(channelId: Int, userId: Int) {
        transactionManager.run {
            val invitationId = it.channelsRepository.getInvitation(channelId, userId) ?: throw AssertionError()
            it.channelsRepository.deleteInvitation(invitationId.channelInvitationId)
        }
    }
}
