package com.example.messagingapp.domain

import org.springframework.stereotype.Component

@Component
class ChannelDomain {
    companion object {
        private val CHANNEL_NAME_LENGTH_RANGE = 4..64
    }

    fun isValidName(channelName: String): Boolean =
            channelName.length in CHANNEL_NAME_LENGTH_RANGE &&
                channelName.all { it.isLetterOrDigit() }

    fun isHigherRole(
        inviterRole: MembershipRole,
        inviteeRole: MembershipRole,
    ): Boolean {
        return inviterRole.value >= inviteeRole.value
    }
}
