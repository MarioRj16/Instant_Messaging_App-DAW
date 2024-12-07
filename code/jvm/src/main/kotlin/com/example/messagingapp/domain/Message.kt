package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class Message(
    val messageId: Int,
    val channelId: Int,
    val sender: User,
    val content: String,
    val createdAt: Instant,
)
