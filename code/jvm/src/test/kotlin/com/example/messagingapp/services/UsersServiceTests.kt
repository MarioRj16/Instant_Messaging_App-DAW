package com.example.messagingapp.services

import com.example.messagingapp.Environment
import com.example.messagingapp.TestClock
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.repository.jdbi.JdbiTransactionManager
import com.example.messagingapp.repository.jdbi.configureWithAppRequirements
import com.example.messagingapp.utils.Either
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class UsersServiceTests {
    @Test
    fun `Registration invitation can be created`() {
        // given: a user service and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a registration invitation
        val inviterId = bootstrapUser()
        val inviteeEmail = generateRandomEmail()

        // then: the registration invitation is created
        when (val inviteResult = userService.createRegistrationInvitation(inviterId)) {
            is Either.Left -> throw AssertionError("Registration invitation creation failed: ${inviteResult.value}")
            is Either.Right -> assertIs<Token>(inviteResult.value)
        }
    }

    @Test
    fun `User can be created`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()
        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }.value

        // when: creating a user
        val username = "username"
        val password = "Password123@"
        val email = generateRandomEmail()

        // then: the user is created
        when (val userResult = userService.createUser(invitationToken.toString(), username, password, email)) {
            is Either.Left -> throw AssertionError("User creation failed: ${userResult.value}")
            is Either.Right -> assertTrue(userResult.value > 0)
        }
    }

    @Test
    fun `User cannot be created with invalid username`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()
        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }.value

        // when: creating a user with an invalid username
        val username = ""
        val password = "Password123@"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser(invitationToken.toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.UsernameIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with unsafe password`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()
        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }.value

        // when: creating a user with an unsafe password
        val username = "username"
        val password = "unsafe"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser(invitationToken.toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.PasswordIsNotSafe)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with invalid email`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()

        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }.value

        // when: creating a user with an invalid email
        val username = "username"
        val password = "Password123@"
        val email = "invalid"

        // then: the user is not created
        when (val userResult = userService.createUser(invitationToken.toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.EmailIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with existing username`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()
        val bootstrappedUser =
            transactionManager.run {
                it.usersRepository.getUserById(inviterId)
            } ?: throw AssertionError("Inviter not found")
        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }.value

        // when: creating a user with an existing username
        val username = bootstrappedUser.username
        val password = "Password123@"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser(invitationToken.toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.UsernameAlreadyExists)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with invalid invitation`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a user with a non-existing invitation
        val username = "username"
        val password = "Password123"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser("invalidToken", username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.InvitationIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with non-existing invitation`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a user with a non-existing invitation
        val username = "username"
        val password = "Password123"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser(UUID.randomUUID().toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.InvitationIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with not pending invitation`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()
        val invitationResult = userService.createRegistrationInvitation(inviterId)
        val invitationToken =
            when (invitationResult) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }
        transactionManager.run {
            it.usersRepository.acceptRegistrationInvitation(invitationToken)
        }

        // when: creating a user with a non-pending invitation
        val username = "username"
        val password = "Password123@"
        val email = generateRandomEmail()

        // then: the user is not created
        when (val userResult = userService.createUser(invitationToken.value.toString(), username, password, email)) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.InvitationIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with expired invitation`() {
        // given: a user service, a user and a registration invitation
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val inviterId = bootstrapUser()

        val invitationToken =
            when (
                val invitationResult = userService.createRegistrationInvitation(inviterId)
            ) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> invitationResult.value
            }
        testClock.advance(365.days)

        // when: creating a user with a non-pending invitation
        val username = generateRandomEmail()
        val password = "Password123@"
        val email = generateRandomEmail()

        val userResult = userService.createUser(invitationToken.value.toString(), username, password, email)

        // then: the user is not created
        when (userResult) {
            is Either.Left -> assertTrue(userResult.value is UserCreationError.InvitationIsNotValid)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `Token can be created`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: creating a token
        val username = user.username
        val password = "Password123@"

        // then: the token is created
        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
            is Either.Right -> assertIs<TokenExternalData>(tokenResult.value)
        }
    }

    @Test
    fun `Token cannot be created with blank username`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: creating a token with invalid user or password
        val username = user.username
        val password = "invalid"

        // then: the token is not created
        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> assertTrue(tokenResult.value is TokenCreationError.UserOrPasswordIsInvalid)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token cannot be created with blank password`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: creating a token with invalid user or password
        val username = user.username
        val password = ""

        // then: the token is not created
        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> assertTrue(tokenResult.value is TokenCreationError.UserOrPasswordIsInvalid)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token cannot be created for non-existent user`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a token with a non-existent user
        val username = generateRandomEmail()
        val password = "Password123@"

        // then: the token is not created
        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> assertTrue(tokenResult.value is TokenCreationError.UserIsNotRegistered)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token cannot be created with wrong password`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: creating a token with wrong password
        val username = user.username
        val password = "wrong"

        // then: the token is not created
        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> assertTrue(tokenResult.value is TokenCreationError.UserOrPasswordIsInvalid)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token can be updated`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: updating a token
        val token =
            when (val tokenResult = userService.createToken(user.username, "Password123@")) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> tokenResult.value.token.value.toString()
            }

        testClock.advance(5.minutes)

        // then: the token is updated
        val updateResult = userService.getUserByToken(token) ?: throw AssertionError("User not found")
        assertEquals(updateResult.userId, user.userId)

        val updatedToken =
            transactionManager.run {
                it.usersRepository.getToken(user.userId)?.token?.value.toString()
            }

        assertEquals(updatedToken, token)
    }

    @Test
    fun `Token cannot be updated if it is expired`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")

        // when: updating a token
        val token =
            when (val tokenResult = userService.createToken(user.username, "Password123@")) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> tokenResult.value.token.value.toString()
            }

        testClock.advance(userDomainConfig.tokenTTL * 2)

        // then: the token is not updated
        val updateResult = userService.getUserByToken(token)
        assertNull(updateResult)
    }

    @Test
    fun `Token cannot be updated if token is in an invalid format`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: updating a token
        val token = "invalidToken"

        // then: the token is not updated
        val updateResult = userService.getUserByToken(token)
        assertEquals(updateResult, null)
    }

    @Test
    fun `Token can be revoked`() {
        // given: a user service and a user
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser()
        val user =
            transactionManager.run {
                it.usersRepository.getUserById(userId)
            } ?: throw AssertionError("User not found")
        val tokenResult = userService.createToken(user.username, "Password123@")

        // when: revoking a token
        val token =
            when (tokenResult) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> tokenResult.value.token.value.toString()
            }

        // then: the token is revoked
        when (val revokeResult = userService.revokeToken(token)) {
            is Either.Left -> throw AssertionError("Token revocation failed: ${revokeResult.value}")
            is Either.Right -> assertTrue(revokeResult.value)
        }
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

        private val userDomainConfig = UserDomainConfig(30.days, 30.minutes, 3, 24.hours)

        private val userDomain =
            UserDomain(
                BCryptPasswordEncoder(),
                UUIDTokenEncoder(),
                userDomainConfig,
            )

        private fun createUsersService(
            testClock: TestClock,
            tokenTTL: Duration = userDomainConfig.tokenTTL,
            tokenRollingTTL: Duration = userDomainConfig.tokenRollingTTL,
            maxTokensPerUser: Int = userDomainConfig.maxTokensPerUser,
            registrationInvitationTTL: Duration = userDomainConfig.registrationInvitationTTL,
        ) = UsersService(
            transactionManager,
            userDomain,
            testClock,
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
