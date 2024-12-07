package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class Channel(
    val channelId: Int,
    val channelName: String,
    val owner: User,
    val createdAt: Instant,
    val isPublic: Boolean,
    val members: List<Membership>,
)
