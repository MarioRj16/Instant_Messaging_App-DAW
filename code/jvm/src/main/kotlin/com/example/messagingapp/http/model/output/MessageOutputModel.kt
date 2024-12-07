package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Message

data class MessageOutputModel(
    val messageId: Int,
    val senderInfo: UserOutputModel,
    val channelId: Int,
    val content: String,
    val createdAt: String,
) {
    constructor(message: Message) : this(
        messageId = message.messageId,
        senderInfo = UserOutputModel(message.sender),
        channelId = message.channelId,
        content = message.content,
        createdAt = message.createdAt.toString(),
    )
}
