package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Membership
import io.swagger.v3.oas.annotations.media.Schema

data class MembershipListOutputModel(
    @Schema(description = "List of channel memberships")
    val memberships: List<MembershipOutputModel>,
    @Schema(description = "Total number of channel memberships", example = "1")
    val size: Int,
) {
    constructor(memberships: List<Membership>) : this(
        memberships.map { MembershipOutputModel(it) },
        memberships.size,
    )
}
