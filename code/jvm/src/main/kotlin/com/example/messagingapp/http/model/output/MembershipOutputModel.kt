package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Membership
import io.swagger.v3.oas.annotations.media.Schema

data class MembershipOutputModel(
    @Schema(description = "Membership ID", example = "1")
    val membershipId: Int,
    @Schema(description = "User information")
    val user: UserOutputModel,
    @Schema(description = "Channel ID", example = "1")
    val channelId: Int,
    @Schema(description = "Role in the channel", example = "MEMBER")
    val role: String,
    @Schema(description = "Date and time of joining the channel", example = "2021-08-01T12:00:00")
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
