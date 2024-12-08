package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation
import io.swagger.v3.oas.annotations.media.Schema

data class ChannelInvitationOutputModel(
    @Schema(description = "The ID of the channel invitation", example = "1")
    val channelInvitationId: Int,
    @Schema(description = "The user who sent the invitation")
    val inviter: UserOutputModel,
    @Schema(description = "The ID of the user who received the invitation", example = "1")
    val inviteeId: Int,
    @Schema(description = "The channel to which the user was invited")
    val channel: ChannelOutputModel,
    @Schema(description = "The role of the user in the channel", example = "MEMBER")
    val role: String,
    @Schema(description = "The date and time when the invitation was sent", example = "2021-01-01T00:00:00")
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
