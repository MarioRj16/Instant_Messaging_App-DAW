package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Message
import io.swagger.v3.oas.annotations.media.Schema

data class MessageOutputModel(
    @Schema(description = "Message ID", example = "1")
    val messageId: Int,
    @Schema(description = "Sender information")
    val senderInfo: UserOutputModel,
    @Schema(description = "Channel ID", example = "1")
    val channelId: Int,
    @Schema(description = "Message content")
    val content: String,
    @Schema(description = "Date and time of sending the message", example = "2021-08-01T12:00:00")
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
