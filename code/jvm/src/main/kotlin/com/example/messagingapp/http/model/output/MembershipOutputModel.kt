package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Membership

data class MembershipOutputModel(
    val membershipId: Int,
    val user: UserOutputModel,
    val channelId: Int,
    val role: String,
    val joinedAt: String,
) {
    constructor(membership: Membership) : this(
        membershipId = membership.membershipId,
        user = UserOutputModel(membership.member),
        channelId = membership.channelId,
        role = membership.role.role,
        joinedAt = membership.joinedAt.toString(),
    )
}
