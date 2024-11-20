package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.TestClock
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.Token
import com.example.messagingapp.generateInvitationCode
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.jdbi
import com.example.messagingapp.runWithHandle
import com.example.messagingapp.userDomain
import com.example.messagingapp.userDomainConfig
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbiUserRepositoryTests {
    @AfterEach
    fun tearDown(): Unit {
        clearDatabase(jdbi)
    }

    @Test
    fun `User can be created and retrieved by id`() {
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)
            val clock = TestClock()

            val userName = "user1"
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)
            val userId = repo.createUser(userName, password, invitationCode)

            val user = repo.getUser(userId)

            assertNotNull(user)
            assertEquals(userId, user.userId)
            assertEquals(userName, user.username)
            assertEquals(password, user.password)
        }
    }

    @Test
    fun `User can be retrieved by ID`() {
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)
            val clock = TestClock()

            val userName = "user1"
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)
            val userId = repo.createUser(userName, password, invitationCode)

            val account = repo.getUser(userId)

            assertNotNull(account)
            assertEquals(userId, account.userId)
            assertEquals(userName, account.username)
            assertEquals(password, account.password)
        }
    }



    @Test
    fun `AuthToken can be retrieved by token`() {
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)
            val clock = TestClock()

            val userName = "user1"
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)
            val userId = repo.createUser(userName, password, invitationCode)
            val currentInstant = TestClock().now()

            val token = AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant)
            repo.createToken(token, userDomainConfig.maxTokensPerUser)

            val authToken = repo.getAuthToken(token.token)

            assertNotNull(authToken)
            assertEquals(userId, authToken.userId)
            assertEquals(token.token, authToken.token)
            /* We don't check the token creation time because we can't guarantee that the token creation time is the
             * same as the token retrieval time due to the time it takes to create the token in the database.
             */
        }
    }

    @Test
    fun `User can be retrieved by username`() {
        runWithHandle { handle ->
            val repo = JdbiUsersRepository(handle)
            val clock = TestClock()

            val userName = "user1"
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)
            repo.createUser(userName, password, invitationCode)

            val account = repo.getUser(userName)

            assertNotNull(account)
            assertEquals(userName, account.username)
            assertEquals(password, account.password)
        }
    }

    @Test
    fun `Registration invitation can be created and retrieved`() {
        runWithHandle {
            val repo = JdbiUsersRepository(it)
            val clock = TestClock()

            val invitationCode = userDomain.createInvitationCode()
            repo.createRegistrationInvitation(clock, invitationCode)

            val invitation = repo.getRegistrationInvitation(invitationCode)

            assertNotNull(invitation)
            assertEquals(invitation.invitationCode, invitationCode)
        }
    }

    @Test
    fun `Registration invitation can be checked for use`() {
        runWithHandle {
            val repo = JdbiUsersRepository(it)
            val clock = TestClock()

            val invitationCode = userDomain.createInvitationCode()
            repo.createRegistrationInvitation(clock, invitationCode)

            var isUsed = repo.registrationInvitationIsUsed(invitationCode)
            assertFalse(isUsed)

            val username = "username"
            val password = "password"
            repo.createUser(username, Password(password), invitationCode)

            isUsed = repo.registrationInvitationIsUsed(invitationCode)
            assertTrue(isUsed)
        }
    }

    @Test
    fun `Token can be created and retrieved`() {
        runWithHandle {
            val repo = JdbiUsersRepository(it)
            val clock = TestClock()

            val userName = "user1"
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)

            val userId = repo.createUser(userName, password, invitationCode)
            val currentInstant = clock.now()

            repo.createToken(
                AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant),
                userDomainConfig.maxTokensPerUser,
            )

            val authToken = repo.getAuthToken(userId)

            assertNotNull(authToken)
        }
    }

    @Test
    fun `Token can be deleted`() {
        runWithHandle {
            val repo = JdbiUsersRepository(it)
            val clock = TestClock()

            val userName = generateRandomString()
            val password = Password("Hash12345@")
            val invitationCode = generateInvitationCode(clock)
            val userId = repo.createUser(userName, password, invitationCode)
            val currentInstant = clock.now()
            val token = AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant)
            repo.createToken(token, userDomainConfig.maxTokensPerUser)

            repo.deleteToken(token.token)

            val authToken = repo.getAuthToken(userId)

            assertNull(authToken)
        }
    }
}
