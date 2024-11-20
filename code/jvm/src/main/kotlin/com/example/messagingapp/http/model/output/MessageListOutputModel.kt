package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Message

data class MessageListOutputModel(
    val messages: List<MessageOutputModel>,
    val total: Int,
) {
    constructor(messages: List<Message>) : this(
        messages.map { MessageOutputModel(it) },
        messages.size,
    )
}
