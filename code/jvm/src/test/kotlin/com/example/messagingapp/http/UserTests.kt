package com.example.messagingapp.http

import com.example.messagingapp.TestClock
import com.example.messagingapp.bootstrapUser
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.generateInvitationCode
import com.example.messagingapp.jdbi
import com.example.messagingapp.transactionManager
import com.example.messagingapp.userDomain
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {
    @AfterEach
    fun tearDown() {
        clearDatabase(jdbi)
    }

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    val baseURL: String
        get() = "http://localhost:$port"

    @Test
    fun `User can be created`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()

        // and: an invitation
        val invitationCode = generateInvitationCode(clock)

        // when: creating an user
        // then: the response is a 201
        client.post().uri(Uris.Users.BASE)
            .bodyValue(
                mapOf(
                    "username" to "username",
                    "password" to "Password123@",
                    "invitationCode" to invitationCode,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        // when: creating a user with the same invitation token
        // then: the response is a 400
        client.post().uri(Uris.Users.BASE)
            .bodyValue(
                mapOf(
                    "username" to "username2",
                    "password" to "Password123@",
                    "invitationCode" to invitationCode,
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an unsafe password
        // then: the response is a 400

        client.post().uri(Uris.Users.BASE)
            .bodyValue(
                mapOf(
                    "username" to "username4",
                    "password" to "unsafe",
                    "invitationCode" to generateInvitationCode(clock),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest

        // when: creating a user with an invalid username
        // then: the response is a 400

        client.post().uri(Uris.Users.BASE)
            .bodyValue(
                mapOf(
                    "username" to "",
                    "password" to "Password123@",
                    "invitationCode" to generateInvitationCode(clock),
                ),
            )
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `User can login`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()

        // and: a user
        val username = "username4"
        val password = "Password123@"
        bootstrapUser(username = username, password = password, testClock = clock)

        // when: logging in
        // then: the response is a 200

        client.post().uri(Uris.Users.LOGIN)
            .bodyValue(
                mapOf(
                    "username" to username,
                    "password" to password,
                ),
            )
            .exchange()
            .expectStatus().isOk

        // when: logging in with an invalid username
        // then: the response is a 400

        client.post().uri(Uris.Users.LOGIN)
            .bodyValue(
                mapOf(
                    "username" to "wrong$username",
                    "password" to password,
                ),
            )
            .exchange()
            .expectStatus().isUnauthorized

        // when: logging in with an invalid password
        // then: the response is a 400

        client.post().uri(Uris.Users.LOGIN)
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
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()

        // and: a user
        val userId = bootstrapUser(testClock = clock)

        val token =
            transactionManager.run {
                val now = clock.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: logging out
        // then: the response is a 204

        client.post().uri(Uris.Users.LOGOUT)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `User can be invite`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()

        // and: a user
        val userId = bootstrapUser(testClock = clock)

        val token =
            transactionManager.run {
                val now = clock.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: inviting a user
        // then: the response is a 201
        val responseBody =
            client.post().uri(Uris.Users.INVITE)
                .header("Authorization", "Bearer $token")
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .returnResult()
                .responseBody

        // when: inviting a user when not logged in
        // then: the response is a 401
        client.post().uri(Uris.Users.INVITE)
            .exchange()
            .expectStatus().isUnauthorized

        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)

        val invitationCode = jsonNode.get("invitationCode").asText()

        // when: registering a user with the invitation code
        // then: the response is a 201
        client.post().uri(Uris.Users.BASE)
            .bodyValue(
                mapOf(
                    "username" to "username",
                    "password" to "Password123@",
                    "invitationCode" to invitationCode,
                ),
            )
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `User can access own profile page`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl(baseURL).build()
        val clock = TestClock()

        // and: a user
        val userId = bootstrapUser(testClock = clock)

        val token =
            transactionManager.run {
                val now = clock.now()
                val token = AuthToken(UUIDTokenEncoder().createToken(), userId, now, now)
                it.usersRepository.createToken(token, userDomain.maxTokensPerUser)
                token.token.value
            }

        // when: accessing the profile page
        // then: the response is a 200

        client.get().uri(Uris.Users.HOME)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // when: accessing the profile page when not logged in
        // then: the response is a 401
        client.get().uri(Uris.Users.HOME)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
