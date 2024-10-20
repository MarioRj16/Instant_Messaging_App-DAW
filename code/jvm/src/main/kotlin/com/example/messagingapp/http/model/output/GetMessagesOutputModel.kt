package com.example.messagingapp.http.model.output

data class GetMessagesOutputModel(
    val messages: List<MessageOutputModel>,
    val total: Int,
) {
    constructor(messages: List<MessageOutputModel>) : this(
        messages,
        messages.size,
    )
}
