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
        private const val ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val PASSWORD_MINIMUM_LENGTH = 8
        private const val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@\$!%*?&]{$PASSWORD_MINIMUM_LENGTH,}\$"

        // https://www.rfc-editor.org/rfc/rfc5321#section-4.5.3
        private val USERNAME_LENGTH_RANGE = 3..64
    }

    val maxTokensPerUser: Int = config.maxTokensPerUser

    fun isValidUsername(username: String): Boolean =
        username.length in USERNAME_LENGTH_RANGE

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

    fun createInvitationCode(): String {
        return (1..config.invitationCodeLength)
            .map { ALLOWED_CHARS.random() }
            .joinToString("")
    }
}
