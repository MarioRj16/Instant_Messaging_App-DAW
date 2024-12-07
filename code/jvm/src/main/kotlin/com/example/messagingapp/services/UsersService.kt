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
            val error = UserCreationError.UsernameIsNotValid
            logger.error(error.message)
            return failure(error)
        }

        if (!usersDomain.isSafePassword(password)) {
            val error = UserCreationError.PasswordIsNotSafe
            logger.error(error.message)
            return failure(error)
        }

        return transactionManager.run {
            if (it.usersRepository.getUser(username) != null) {
                val error = UserCreationError.UsernameAlreadyExists
                logger.error(error.message)
                return@run failure(error)
            }

            val registrationInvitation = it.usersRepository.getRegistrationInvitation(invitationCode)
            if (
                registrationInvitation == null ||
                it.usersRepository.registrationInvitationIsUsed(invitationCode)
            ) {
                val error = UserCreationError.InvitationCodeNotValid
                logger.error(error.message)
                return@run failure(error)
            }

            val userId =
                it.usersRepository.createUser(username, usersDomain.hashPassword(password), invitationCode)
            success(userId)
        }
    }

    fun getUserByToken(token: String): User? {
        logger.info("Getting user by token: $token")
        return transactionManager.run {
            try {
                val tokenValue = Token(UUID.fromString(token))
                val authToken = it.usersRepository.getAuthToken(tokenValue) ?: return@run null

                return@run if (usersDomain.isTokenExpired(clock, authToken)) {
                    logger.error("Token is expired")
                    null
                } else {
                    it.usersRepository.updateToken(tokenValue)
                    it.usersRepository.getUser(authToken.userId)
                }
            } catch (e: IllegalArgumentException) {
                logger.error("Token is not valid")
                null
            }
        }
    }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        logger.info("Creating token for user: $username")
        return transactionManager.run {
            val user = it.usersRepository.getUser(username)
            if (user == null) {
                val error = TokenCreationError.UserOrPasswordIsInvalid
                logger.error(error.message)
                return@run failure(error)
            }

            return@run if (usersDomain.verifyPassword(password, user.password)) {
                val token = Token(UUID.randomUUID())
                val now = clock.now()
                val authToken = AuthToken(token, user.userId, now, now)
                it.usersRepository.createToken(authToken, usersDomain.maxTokensPerUser)
                success(TokenExternalData(token, usersDomain.getTokenExpiration(authToken)))
            } else {
                val error = TokenCreationError.UserOrPasswordIsInvalid
                logger.error(error.message)
                failure(error)
            }
        }
    }

    fun revokeToken(token: String): TokenRevocationResult {
        logger.info("Revoking token: $token")
        return transactionManager.run {
            try {
                val tokenValue = Token(UUID.fromString(token))
                if (it.usersRepository.deleteToken(tokenValue)) {
                    success(true)
                } else {
                    val error = TokenRevocationError.TokenIsNotValid
                    logger.error(error.message)
                    failure(error)
                }
            } catch (e: IllegalArgumentException) {
                val error = TokenRevocationError.TokenIsNotValid
                logger.error(error.message)
                return@run failure(error)
            }
        }
    }

    fun createRegistrationInvitation(): RegistrationInvitationResult {
        logger.info("Creating registration invitation")
        return transactionManager.run {
            var invitationCode = usersDomain.createInvitationCode()
            while (it.usersRepository.getRegistrationInvitation(invitationCode) != null) {
                // Ensure the invitation code is unique
                invitationCode = usersDomain.createInvitationCode()
            }

            logger.info("Invitation code created: $invitationCode")
            it.usersRepository.createRegistrationInvitation(clock, invitationCode)
            success(invitationCode)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersService::class.java)
    }
}
