package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Membership

data class MembershipListOutputModel(
    val memberships: List<MembershipOutputModel>,
    val size: Int,
) {
    constructor(memberships: List<Membership>) : this(
        memberships.map { MembershipOutputModel(it) },
        memberships.size,
    )
}
