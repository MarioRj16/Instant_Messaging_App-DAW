package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation
import io.swagger.v3.oas.annotations.media.Schema

class ListInvitationsOutputModel(
    @Schema(description = "List of channel invitations")
    val invitations: List<ChannelInvitationOutputModel>,
    @Schema(description = "Total number of channel invitations", example = "1")
    val size: Int,
) {
    constructor(invitations: List<ChannelInvitation>) : this(
        invitations.map { ChannelInvitationOutputModel(it) },
        invitations.size,
    )
}
