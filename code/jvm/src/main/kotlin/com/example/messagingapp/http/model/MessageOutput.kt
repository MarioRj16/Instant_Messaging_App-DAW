package com.example.messagingapp.http.model

data class MessageOutput(
    val messageId: Int,
    val senderInfo: SenderInfoOutput,
    val channelId: Int,
    val content: String,
    val createdAt: Long,
)
