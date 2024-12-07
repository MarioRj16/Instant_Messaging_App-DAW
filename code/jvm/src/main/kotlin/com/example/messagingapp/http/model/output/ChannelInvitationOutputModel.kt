package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation

data class ChannelInvitationOutputModel(
    val channelInvitationId: Int,
    val inviter: UserOutputModel,
    val inviteeId: Int,
    val channel: ChannelOutputModel,
    val role: String,
    val createdAt: String,
) {
    constructor(invitation: ChannelInvitation) : this(
        invitation.channelInvitationId,
        UserOutputModel(invitation.inviter),
        invitation.inviteeId,
        ChannelOutputModel(invitation.channel),
        invitation.role.role,
        invitation.createdAt.toString(),
    )
}
