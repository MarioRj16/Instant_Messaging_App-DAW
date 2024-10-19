package com.example.messagingapp.domain

data class Message(
    val messageId: Int,
    val channel: Channel,
    val sender: User,
    val message: String,
    val createdAt: Long,
)
