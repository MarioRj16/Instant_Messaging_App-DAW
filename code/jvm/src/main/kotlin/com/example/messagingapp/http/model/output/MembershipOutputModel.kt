package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.MembershipRole

data class MembershipOutputModel(
    val membershipId: Int,
    val userId: Int,
    val channelId: Int,
    val role: MembershipRole,
    val joinedAt: Long,
)
