package com.example.messagingapp.domain

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

@Component
class ChannelDomain {
    companion object {
        private const val NAME_MIN_LENGTH = 3
        private const val NAME_MAX_LENGTH = 40
        private const val INVITATION_EXPIRATION_DAYS = 7L
    }

    fun isValidName(channelName: String): Boolean {
        return channelName.isNotBlank() &&
            channelName.length in NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }

    fun isHigherRole(
        inviterRole: MembershipRole,
        inviteeRole: MembershipRole,
    ): Boolean {
        return inviterRole.value >= inviteeRole.value
    }

    fun isExpired(expirationDate: Long): Boolean {
        return expirationDate < Clock.System.now().epochSeconds
    }

    val expirationDate
        get() = Clock.System.now().epochSeconds + INVITATION_EXPIRATION_DAYS * 24 * 60 * 60
}
