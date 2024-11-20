package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class ChannelInvitation(
    val channelInvitationId: Int,
    val inviterId: Int,
    val inviteeId: Int,
    val channelId: Int,
    val role: InviteRole,
    val createdAt: Instant,
)
