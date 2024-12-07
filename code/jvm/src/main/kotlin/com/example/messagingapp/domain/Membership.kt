package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class Membership(
    val membershipId: Int,
    val member: User,
    val channelId: Int,
    val role: MembershipRole,
    val joinedAt: Instant,
)
