package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Message
import io.swagger.v3.oas.annotations.media.Schema

data class MessageListOutputModel(
    @Schema(description = "List of messages")
    val messages: List<MessageOutputModel>,
    @Schema(description = "Total number of messages", example = "1")
    val total: Int,
) {
    constructor(messages: List<Message>) : this(
        messages.map { MessageOutputModel(it) },
        messages.size,
    )
}
