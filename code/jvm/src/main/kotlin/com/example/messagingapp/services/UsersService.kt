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
        inviteToken: String,
        username: String,
        password: String,
        email: String,
    ): UserCreationResult {
        logger.info("Creating user with username: $username and email: $email")

        if (!usersDomain.isValidUsername(username)) {
            logger.error("Username validation failed for: $username")
            return failure(UserCreationError.UsernameIsNotValid)
        }

        if (!usersDomain.isSafePassword(password)) {
            logger.error("Password validation failed")
            return failure(UserCreationError.PasswordIsNotSafe)
        }

        if (!usersDomain.isValidEmail(email)) {
            logger.error("Email validation failed for: $email")
            return failure(UserCreationError.EmailIsNotValid)
        }

        return transactionManager.run {
            logger.info("Transaction started for user creation")
            if (it.usersRepository.getUserByUsername(username) != null) {
                return@run failure(UserCreationError.UsernameAlreadyExists)
            }

            if (it.usersRepository.getUserByEmail(email) != null) {
                logger.error("Username already exists: $username")
                return@run failure(UserCreationError.EmailAlreadyExists)
            }

            val token =
                try {
                    Token(UUID.fromString(inviteToken))
                } catch (e: IllegalArgumentException) {
                    logger.error("Invalid invitation token: $inviteToken", e)
                    return@run failure(UserCreationError.InvitationIsNotValid)
                }

            val registrationInvitation = it.usersRepository.getRegistrationInvitation(token)
            if (registrationInvitation == null) {
                logger.error("No registration invitation found for token: $inviteToken")
                return@run failure(UserCreationError.InvitationIsNotValid)
            }

            return@run if (usersDomain.isRegistrationInvitationValid(clock, registrationInvitation)) {
                /**
                 * We handle user creation and invitation acceptance in the same function for several reasons:
                 *
                 * 1. Transactional Integrity: Performing both operations in a single transaction ensures atomicity. If any step fails
                 *    (e.g., invalid or already accepted invitation), the transaction rolls back, maintaining data consistency and reducing
                 *    the risk of race conditions.
                 *
                 * 2. Simplified Time Management: Keeping these steps together avoids the complexity of tracking time between user
                 *    creation and invitation acceptance, especially since we validate invitation expiration. This allows for immediate
                 *    acceptance after user creation without managing time-sensitive states.
                 *
                 * 3. Simplicity: This approach simplifies the flow of the registration process by minimizing the need for additional
                 *    orchestration between functions. As invitation acceptance always follows user creation, separating them is unnecessary
                 *    unless future requirements dictate otherwise.
                 *
                 * Overall, combining these operations is pragmatic for our current use case, ensuring they are handled cohesively in one
                 * transactional unit.
                 */
                val accountId =
                    it.usersRepository.createUser(
                        username,
                        email,
                        usersDomain.hashPassword(password),
                    )
                it.usersRepository.acceptRegistrationInvitation(token)
                logger.info("User created with accountId: $accountId and invitation accepted.")
                success(accountId)
            } else {
                logger.error("Invitation is not valid for token: $inviteToken")
                failure(UserCreationError.InvitationIsNotValid)
            }
        }
    }

    fun getUserByToken(token: String): User? =
        transactionManager.run {
            try {
                val tokenValue = Token(UUID.fromString(token)) // Convert token to UUID
                val authToken = it.usersRepository.getUserByToken(tokenValue) ?: return@run null // Retrieve token or return null

                // Check if the token is expired
                return@run if (usersDomain.isTokenExpired(clock, authToken)) {
                    null // Token expired, return null
                } else {
                    // Token is valid, update the token and return the user
                    it.usersRepository.updateToken(tokenValue)
                    it.usersRepository.getUserById(authToken.userId)
                }
            } catch (e: IllegalArgumentException) {
                null // Handle invalid UUID format or token parsing issues
            }
        }

    fun createToken(
        username: String,
        password: String,
    ): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            return failure(TokenCreationError.UserOrPasswordIsInvalid)
        }
        return transactionManager.run {
            val user =
                it.usersRepository.getUserByUsername(username)
                    ?: return@run failure(TokenCreationError.UserIsNotRegistered)
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

    fun createRegistrationInvitation(inviterId: Int): RegistrationInvitationResult =
        transactionManager.run {
            val token = it.usersRepository.createRegistrationInvitation(inviterId, clock.now())
            success(token)
        }

    companion object {
        private val logger = LoggerFactory.getLogger(UsersService::class.java)
    }
}
