package com.example.messagingapp.domain

import kotlin.time.Duration

data class UserDomainConfig(
    val tokenTTL: Duration,
    val tokenRollingTTL: Duration,
    val maxTokensPerUser: Int,
    val registrationInvitationTTL: Duration,
    val invitationCodeLength: Int,
) {
    init {
        require(tokenTTL.isPositive()) { "Token TTL must be greater than zero" }
        require(tokenRollingTTL.isPositive()) { "Token Rolling TTL must be greater than zero" }
        require(maxTokensPerUser > 0) { "Max tokens per user must be greater than zero" }
        require(registrationInvitationTTL.isPositive()) { "Registration Invitation TTL must be greater than zero" }
        require(invitationCodeLength > 0) { "Invitation code length must be greater than zero" }
    }
}
