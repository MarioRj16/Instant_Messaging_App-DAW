package com.example.messagingapp.http.model.output

class GetMembershipsOutputModel(
    val memberships: List<MembershipOutputModel>,
    val size: Int,
) {
    constructor(memberships: List<MembershipOutputModel>) : this(
        memberships,
        memberships.size,
    )
}
