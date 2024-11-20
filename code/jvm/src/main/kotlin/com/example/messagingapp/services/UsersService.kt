package com.example.messagingapp.services

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.repository.TransactionManager
import com.example.messagingapp.utils.failure
import com.example.messagingapp.utils.success
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UserDomain,
    private val clock: Clock,
) {
    fun createUser(
        invitationCode: String,
        username: String,
        password: String,
    ): UserCreationResult {
        logger.info("Creating user with username: $username")

        if (!usersDomain.isValidUsername(username)) {
            logger.error("Username validation failed for: $username")
            return failure(UserCreationError.UsernameIsNotValid)
        }

        if (!usersDomain.isSafePassword(password)) {
            logger.error("Password validation failed")
            return failure(UserCreationError.PasswordIsNotSafe)
        }

        return transactionManager.run {
            if (it.usersRepository.getUser(username) != null) {
                logger.error("Username already exists: $username")
                return@run failure(UserCreationError.UsernameAlreadyExists)
            }


            val registrationInvitation = it.usersRepository.getRegistrationInvitation(invitationCode)
            if (
                registrationInvitation == null ||
                it.usersRepository.registrationInvitationIsUsed(invitationCode)
            ) {
                logger.error("Invitation is not valid: $invitationCode")
                return@run failure(UserCreationError.InvitationCodeNotValid)
            }

            val userId =
                it.usersRepository.createUser(username, usersDomain.hashPassword(password), invitationCode)
            success(userId)
        }
    }

    fun getUserByToken(token: String): User? =
        transactionManager.run {
            try {
                val tokenValue = Token(UUID.fromString(token)) // Convert token to UUID
                val authToken = it.usersRepository.getAuthToken(tokenValue) ?: return@run null // Retrieve token or return null

                // Check if the token is expired
                return@run if (usersDomain.isTokenExpired(clock, authToken)) {
                    null // Token expired, return null
                } else {
                    // Token is valid, update the token and return the user
                    it.usersRepository.updateToken(tokenValue)
                    it.usersRepository.getUser(authToken.userId)
                }
            } catch (e: IllegalArgumentException) {
                null // Handle invalid UUID format or token parsing issues
            }
        }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        return transactionManager.run {
            val user =
                it.usersRepository.getUser(username)
                    ?: return@run failure(TokenCreationError.UserOrPasswordIsInvalid)
            return@run if (usersDomain.verifyPassword(password, user.password)) {
                val token = Token(UUID.randomUUID())
                val now = clock.now()
                val authToken = AuthToken(token, user.userId, now, now)
                it.usersRepository.createToken(authToken, usersDomain.maxTokensPerUser)
                success(TokenExternalData(token, usersDomain.getTokenExpiration(authToken)))
            } else {
                failure(TokenCreationError.UserOrPasswordIsInvalid)
            }
        }
    }

    fun revokeToken(token: String): TokenRevocationResult {
        return transactionManager.run {
            try {
                val tokenValue = Token(UUID.fromString(token))
                if (it.usersRepository.deleteToken(tokenValue)) {
                    success(true)
                } else {
                    failure(TokenRevocationError.TokenIsNotValid)
                }
            } catch (e: IllegalArgumentException) {
                return@run failure(TokenRevocationError.TokenIsNotValid)
            }
        }
    }

    fun createRegistrationInvitation(): RegistrationInvitationResult =
        transactionManager.run {
            var invitationCode = usersDomain.createInvitationCode()
            while (it.usersRepository.getRegistrationInvitation(invitationCode) != null){
                // Ensure the invitation code is unique
                invitationCode = usersDomain.createInvitationCode()
            }

            it.usersRepository.createRegistrationInvitation(clock, invitationCode)
            success(invitationCode)
        }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersService::class.java)
    }
}
