package com.example.messagingapp.services

import com.example.messagingapp.TestClock
import com.example.messagingapp.bootstrapUser
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.jdbi
import com.example.messagingapp.transactionManager
import com.example.messagingapp.userDomain
import com.example.messagingapp.userDomainConfig
import com.example.messagingapp.utils.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UsersServiceTests {

    @AfterEach
    fun tearDown() {
        clearDatabase(jdbi)
    }

    @Test
    fun `Registration invitation can be created`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        when (val inviteResult = userService.createRegistrationInvitation()) {
            is Either.Left -> throw AssertionError("Registration invitation creation failed: ${inviteResult.value}")
            is Either.Right -> assertIs<String>(inviteResult.value)
        }
    }

    @Test
    fun `User can be created`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val invitationToken =
            when (val invitationResult = userService.createRegistrationInvitation()) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> assertIs<String>(invitationResult.value)
            }

        val username = "username"
        val password = "Password123@"

        when (val userResult = userService.createUser(invitationToken, username, password)) {
            is Either.Left -> throw AssertionError("User creation failed: ${userResult.value}")
            is Either.Right -> assertIs<Int>(userResult.value)
        }
    }

    @Test
    fun `User cannot be created with invalid username`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val invitationToken =
            when (val invitationResult = userService.createRegistrationInvitation()) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> assertIs<String>(invitationResult.value)
            }

        val username = ""
        val password = "Password123@"

        when (val userResult = userService.createUser(invitationToken, username, password)) {
            is Either.Left -> assertIs<UserCreationError.UsernameIsNotValid>(userResult.value)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with unsafe password`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val invitationToken =
            when (val invitationResult = userService.createRegistrationInvitation()) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> assertIs<String>(invitationResult.value)
            }

        val username = "username"
        val password = "unsafe"

        when (val userResult = userService.createUser(invitationToken, username, password)) {
            is Either.Left -> assertIs<UserCreationError.PasswordIsNotSafe>(userResult.value)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with existing username`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val username = "username"
        val password = "Password123@"
        bootstrapUser(username, password, testClock)

        val invitationToken =
            when (val invitationResult = userService.createRegistrationInvitation()) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> assertIs<String>(invitationResult.value)
            }

        when (val userResult = userService.createUser(invitationToken, username, password)) {
            is Either.Left -> assertIs<UserCreationError.UsernameAlreadyExists>(userResult.value)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created if invitation is not found`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val invalidInvitationCode = "invalidToken"
        val username = "username"
        val password = "Password123"

        when (val userResult = userService.createUser(invalidInvitationCode, username, password)) {
            is Either.Left -> assertIs<UserCreationError.InvitationCodeNotValid>(userResult.value)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `User cannot be created with used invitationCode`(){
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val invitationToken =
            when (val invitationResult = userService.createRegistrationInvitation()) {
                is Either.Left -> throw AssertionError("Registration invitation not found: ${invitationResult.value}")
                is Either.Right -> assertIs<String>(invitationResult.value)
            }

        val username = "username"
        val password = "Password123@"

        when (val userResult = userService.createUser(invitationToken, username, password)) {
            is Either.Left -> throw AssertionError("User creation failed: ${userResult.value}")
            is Either.Right -> assertIs<Int>(userResult.value)
        }

        when (val userResult = userService.createUser(invitationToken, "${username}2", password)) {
            is Either.Left -> assertIs<UserCreationError.InvitationCodeNotValid>(userResult.value)
            is Either.Right -> throw AssertionError("User creation should have failed")
        }
    }

    @Test
    fun `Token can be created`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val username = "username"
        val password = "Password123@"
        bootstrapUser(username, password, testClock)

        when (val tokenResult = userService.createToken(username, password)) {
            is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
            is Either.Right -> assertIs<TokenExternalData>(tokenResult.value)
        }
    }

    @Test
    fun `Token cannot be created for wrong username`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val username = "username"
        val password = "Password123@"
        bootstrapUser(username, password, testClock)

        val wrongUsername = "$username-wrong"
        when (val tokenResult = userService.createToken(wrongUsername, password)) {
            is Either.Left -> assertIs<TokenCreationError.UserOrPasswordIsInvalid>(tokenResult.value)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token cannot be created with wrong password`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val username = "username"
        val password = "Password123@"
        bootstrapUser(username, password, testClock)

        val wrongPassword = "$password-wrong"

        when (val tokenResult = userService.createToken(username, wrongPassword)) {
            is Either.Left -> assertIs<TokenCreationError.UserOrPasswordIsInvalid>(tokenResult.value)
            is Either.Right -> throw AssertionError("Token creation should have failed")
        }
    }

    @Test
    fun `Token can be updated`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val username = "username"
        val password = "Password123@"
        val userId = bootstrapUser(username, password, testClock)

        val token =
            when (val tokenResult = userService.createToken(username, password)) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> assertIs<TokenExternalData>(tokenResult.value)
            }.token.value.toString()

        testClock.advance(userDomainConfig.tokenRollingTTL / 2)

        val updateResult = userService.getUserByToken(token) ?: throw AssertionError("User not found")
        assertEquals(updateResult.userId, userId)


        val updatedToken =
            transactionManager.run {
                it.usersRepository.getAuthToken(userId)?.token?.value.toString()
            }

        assertEquals(updatedToken, token)
    }

    @Test
    fun `Token cannot be updated if it is expired`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val user =
            transactionManager.run {
                it.usersRepository.getUser(userId)
            } ?: throw AssertionError("User not found")

        val token =
            when (val tokenResult = userService.createToken(user.username, "Password123@")) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> assertIs<TokenExternalData>(tokenResult.value)
            }.token.value.toString()

        testClock.advance(userDomainConfig.tokenTTL * 2)

        val updateResult = userService.getUserByToken(token)
        assertNull(updateResult)
    }

    @Test
    fun `Token cannot be updated if token is in an invalid format`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        val token = "invalidToken"

        val updatedToken = userService.getUserByToken(token)
        assertNull(updatedToken)
    }

    @Test
    fun `Token can be revoked`() {
        val testClock = TestClock()
        val userService = createUsersService(testClock)
        val username = "username"
        val password = "Password123@"
        bootstrapUser(username, password, testClock)

        val token =
            when (val tokenResult = userService.createToken(username, password)) {
                is Either.Left -> throw AssertionError("Token creation failed: ${tokenResult.value}")
                is Either.Right -> tokenResult.value.token.value.toString()
            }

        when (val revokeResult = userService.revokeToken(token)) {
            is Either.Left -> throw AssertionError("Token revocation failed: ${revokeResult.value}")
            is Either.Right -> assertTrue(revokeResult.value)
        }
    }

    companion object {
        private fun createUsersService(
            testClock: TestClock,
        ) = UsersService(
            transactionManager,
            userDomain,
            testClock,
        )
    }
}
