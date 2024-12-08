package com.example.messagingapp.domain

import org.springframework.stereotype.Component

@Component
class ChannelDomain {
    companion object {
        private const val CHANNEL_NAME_REGEX = "^[a-zA-Z0-9_]{4,64}\$"
    }

    fun isValidName(channelName: String): Boolean = CHANNEL_NAME_REGEX.toRegex().matches(channelName)

    fun isHigherRole(
        inviterRole: MembershipRole,
        inviteeRole: MembershipRole,
    ): Boolean {
        return inviterRole.value >= inviteeRole.value
    }
}
