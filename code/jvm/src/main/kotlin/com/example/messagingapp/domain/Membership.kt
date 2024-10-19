package com.example.messagingapp.domain

import java.time.LocalDateTime

data class Membership(
    val membershipId: Int,
    val member: User,
    val channel: Channel,
    val role: MembershipRole,
    val joinedAt: LocalDateTime,
)
