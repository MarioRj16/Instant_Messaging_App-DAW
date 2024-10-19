package com.example.messagingapp.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UserDomainConfig,
) {
    companion object {
        private const val USERNAME_MIN_LENGTH = 4
        private const val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,}$"
        private const val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@\$!%*?&]{8,}\$"
    }

    fun isValidUsername(username: String): Boolean = username.length >= USERNAME_MIN_LENGTH

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.toRegex().matches(email)

    fun hashPassword(password: String): Password = Password(passwordEncoder.encode(password))

    fun verifyPassword(
        password: String,
        hash: Password,
    ): Boolean = passwordEncoder.matches(password, hash.value)

    fun isSafePassword(password: String): Boolean = PASSWORD_REGEX.toRegex().matches(password)

    fun isTokenExpired(
        clock: Clock,
        token: AuthToken,
    ): Boolean {
        return getTokenExpiration(token) <= clock.now()
    }

    fun getTokenExpiration(token: AuthToken): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTTL
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTTL
        return minOf(absoluteExpiration, rollingExpiration)
    }

    fun createToken(): Token = tokenEncoder.createToken()

    val maxTokensPerUser = config.maxTokensPerUser

    fun isRegistrationInvitationTimeValid(
        clock: Clock,
        invitation: RegistrationInvitation,
    ): Boolean {
        val now = clock.now()
        return invitation.createdAt <= now &&
            (now - invitation.createdAt) <= config.registrationInvitationTTL
    }

    fun isRegistrationInvitationValid(
        clock: Clock,
        invitation: RegistrationInvitation,
    ): Boolean =
        isRegistrationInvitationTimeValid(clock, invitation) &&
            (invitation.invitationStatus == InviteStatus.PENDING)
}
