package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation

data class ChannelInvitationOutputModel(
    val channelInvitationId: Int,
    val inviterId: Int,
    val inviteeId: Int,
    val channelId: Int,
    val role: String,
    val createdAt: String,
){
    constructor(invitation: ChannelInvitation): this(
        invitation.channelInvitationId,
        invitation.inviterId,
        invitation.inviteeId,
        invitation.channelId,
        invitation.role.role,
        invitation.createdAt.toString()
    )
}
