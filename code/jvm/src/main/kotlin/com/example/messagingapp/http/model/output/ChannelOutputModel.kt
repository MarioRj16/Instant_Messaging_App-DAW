package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel

data class ChannelOutputModel(
    val channelId: Int,
    val channelName: String,
    val ownerId: Int,
    val createdAt: String,
    val isPublic: Boolean,
) {
    constructor(channel: Channel): this(
        channel.channelId,
        channel.channelName,
        channel.ownerId,
        channel.createdAt.toString(),
        channel.isPublic
    )
}