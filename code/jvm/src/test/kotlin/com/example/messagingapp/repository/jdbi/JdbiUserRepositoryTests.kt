package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.Environment
import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.InviteStatus
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomString
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.hours

class JdbiUserRepositoryTests {
    @Test
    fun `User can be created and retrieved by id`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // when: creating a user
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val userId = repo.createUser(userName, email, password)

            // and: retrieving a user
            val account = repo.getUserById(userId)

            // then: the user is stored
            assertNotNull(account)
            assertEquals(userName, account.username)
            assertEquals(email, account.email)
            assertEquals(password, account.password)
        }
    }

    @Test
    fun `User can be retrieved by id`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // when: creating a user
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            repo.createUser(userName, email, password)

            // and: retrieving a user
            val account = repo.getUserByUsername(userName)

            // then: the user is stored
            assertNotNull(account)
            assertEquals(userName, account.username)
            assertEquals(email, account.email)
            assertEquals(password, account.password)
        }
    }

    @Test
    fun `User can be retrieved by email`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // when: creating a user
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            repo.createUser(userName, email, password)

            // and: retrieving a user
            val account = repo.getUserByEmail(email)

            // then: the user is stored
            assertNotNull(account)
            assertEquals(userName, account.username)
            assertEquals(email, account.email)
            assertEquals(password, account.password)
        }
    }

    @Test
    fun `AuthToken can be retrieved by token`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // when: creating a user and a token
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val userId = repo.createUser(userName, email, password)
            val currentInstant = Clock.System.now()
            val token = AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant)
            repo.createToken(token, usersDomainDefaultConfig.maxTokensPerUser)

            // and: retrieving a user
            val account = repo.getUserByToken(token.token)

            // then: the user is stored
            assertNotNull(account)
            assertEquals(userId, account.userId)
            assertEquals(token.token, account.token)
            /* We don't check the token creation time because we can't guarantee that the token creation time is the
             * same as the token retrieval time due to the time it takes to create the token in the database.
             */
        }
    }

    @Test
    fun `User can be retrieved by username`() {
        runWithHandle { handle ->
            // given: a UsersRepository
            val repo = JdbiUsersRepository(handle)

            // when: creating a user
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            repo.createUser(userName, email, password)

            // and: retrieving a user
            val account = repo.getUserByUsername(userName)

            // then: the user is stored
            assertNotNull(account)
            assertEquals(userName, account.username)
            assertEquals(email, account.email)
            assertEquals(password, account.password)
        }
    }

    @Test
    fun `Registration invitation can be created and retrieved`() {
        runWithHandle {
            // given: a UsersRepository and a user
            val repo = JdbiUsersRepository(it)
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val inviterId = repo.createUser(userName, email, password)

            // when: storing a registration invitation
            val invitationToken = repo.createRegistrationInvitation(inviterId, Clock.System.now())

            // and: retrieving the registration invitation
            val invitation = repo.getRegistrationInvitation(invitationToken)

            // then: the registration invitation is stored
            assertNotNull(invitation)
            assertEquals(inviterId, invitation.inviterId)
            assertEquals(invitation.invitationStatus, InviteStatus.PENDING)
        }
    }

    @Test
    fun `Registration invitation can be accepted`() {
        runWithHandle {
            // given: a UsersRepository and a user
            val repo = JdbiUsersRepository(it)
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val inviterId = repo.createUser(userName, email, password)

            // when: storing a registration invitation
            val invitationToken = repo.createRegistrationInvitation(inviterId, Clock.System.now())

            // and: retrieving, accepting and retrieving the registration invitation
            repo.getRegistrationInvitation(invitationToken)
            repo.acceptRegistrationInvitation(invitationToken)
            val invitationAccepted = repo.getRegistrationInvitation(invitationToken)

            // then: the registration invitation is stored
            assertNotNull(invitationAccepted)
            assertEquals(inviterId, invitationAccepted.inviterId)
            assertEquals(invitationAccepted.invitationStatus, InviteStatus.ACCEPTED)
        }
    }

    @Test
    fun `Registration invitation can be declined`() {
        runWithHandle {
            // given: a UsersRepository and a user
            val repo = JdbiUsersRepository(it)
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val inviterId = repo.createUser(userName, email, password)

            // when: storing a registration invitation
            val invitationToken = repo.createRegistrationInvitation(inviterId, Clock.System.now())

            // and: retrieving, declining and retrieving the registration invitation
            repo.getRegistrationInvitation(invitationToken)
            repo.declineRegistrationInvitation(invitationToken)
            val invitationDeclined = repo.getRegistrationInvitation(invitationToken)

            // then: the registration invitation is stored
            assertNotNull(invitationDeclined)
            assertEquals(inviterId, invitationDeclined.inviterId)
            assertEquals(invitationDeclined.invitationStatus, InviteStatus.REJECTED)
        }
    }

    @Test
    fun `Token can be created and retrieved`() {
        runWithHandle {
            // given: a UsersRepository and a user
            val repo = JdbiUsersRepository(it)
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val userId = repo.createUser(userName, email, password)
            val currentInstant = Clock.System.now()

            // when: creating a token
            repo.createToken(
                AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant),
                usersDomainDefaultConfig.maxTokensPerUser,
            )

            // and: retrieving the token
            val authToken = repo.getToken(userId)

            // then: the token is stored
            assertNotNull(authToken)
        }
    }

    @Test
    fun `Token can be deleted`() {
        runWithHandle {
            // given: a UsersRepository and a user
            val repo = JdbiUsersRepository(it)
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            val userId = repo.createUser(userName, email, password)
            val currentInstant = Clock.System.now()
            val token = AuthToken(Token(UUID.randomUUID()), userId, currentInstant, currentInstant)
            repo.createToken(token, usersDomainDefaultConfig.maxTokensPerUser)

            // when: deleting the token
            repo.deleteToken(token.token)

            // and: retrieving the token
            val authToken = repo.getToken(userId)

            // then: the token is deleted
            assertNull(authToken)
        }
    }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val usersDomainDefaultConfig =
            UserDomainConfig(
                tokenTTL = 24.hours,
                tokenRollingTTL = 1.hours,
                maxTokensPerUser = 3,
                registrationInvitationTTL = 24.hours,
            )

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
    }
}
