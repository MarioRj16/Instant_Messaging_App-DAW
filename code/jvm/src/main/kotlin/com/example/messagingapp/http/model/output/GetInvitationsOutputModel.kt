package com.example.messagingapp.http.model.output

class GetInvitationsOutputModel(
    val invitations: List<ChannelInvitationOutputModel>,
    val size: Int,
) {
    constructor(invitations: List<ChannelInvitationOutputModel>) : this(
        invitations,
        invitations.size,
    )
}
