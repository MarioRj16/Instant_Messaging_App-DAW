package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class ChannelInvitation(
    val channelInvitationId: Int,
    val inviter: User,
    val inviteeId: Int,
    val channel: Channel,
    val role: InviteRole,
    val createdAt: Instant,
)
