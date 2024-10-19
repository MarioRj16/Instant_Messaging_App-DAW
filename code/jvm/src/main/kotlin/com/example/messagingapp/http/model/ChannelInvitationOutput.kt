package com.example.messagingapp.http.model

import com.example.messagingapp.domain.InviteRole
import com.example.messagingapp.domain.InviteStatus

data class ChannelInvitationOutput(
    val channelInvitationId: Int,
    val inviterId: Int,
    val inviteeId: Int,
    val channelId: Int,
    val role: InviteRole,
    val createdAt: Long,
    val expiresAt: Long,
    val status: InviteStatus,
)
