package com.example.messagingapp.http.model

import com.example.messagingapp.domain.MembershipRole

data class MembershipOutput(
    val membershipId: Int,
    val userId: Int,
    val channelId: Int,
    val role: MembershipRole,
    val joinedAt: Long,
)
