package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation

class ListInvitationsOutputModel(
    val invitations: List<ChannelInvitationOutputModel>,
    val size: Int,
) {
    constructor(invitations: List<ChannelInvitation>) : this(
        invitations.map { ChannelInvitationOutputModel(it) },
        invitations.size,
    )
}
