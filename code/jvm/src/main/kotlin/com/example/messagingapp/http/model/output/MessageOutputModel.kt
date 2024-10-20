package com.example.messagingapp.http.model.output

data class MessageOutputModel(
    val messageId: Int,
    val senderInfo: SenderInfoOutputModel,
    val channelId: Int,
    val content: String,
    val createdAt: String,
)
