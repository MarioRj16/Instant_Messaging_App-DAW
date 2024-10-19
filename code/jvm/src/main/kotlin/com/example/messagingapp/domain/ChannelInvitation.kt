package com.example.messagingapp.domain

import java.time.LocalDateTime

data class ChannelInvitation(
    val channelInvitationId: Int,
    val inviter: User,
    val invitee: User,
    val channel: Channel,
    val role: InviteRole,
    val createdAt: LocalDateTime,
    val inviteStatus: InviteStatus,
) {
    val isExpired: Boolean
        get() = createdAt.plusDays(INVITATION_EXPIRATION_DAYS).isBefore(LocalDateTime.now())

    companion object {
        const val INVITATION_EXPIRATION_DAYS = 7L
    }
}
