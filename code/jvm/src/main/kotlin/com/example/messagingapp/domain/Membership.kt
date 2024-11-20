package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class Membership(
    val membershipId: Int,
    val memberId: Int,
    val channelId: Int,
    val role: MembershipRole,
    val joinedAt: Instant,
)
