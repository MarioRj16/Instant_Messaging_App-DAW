package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel

class ChannelListOutputModel(
    val channels: List<ChannelOutputModel>,
    val size: Int,
) {
    constructor(channels: List<Channel>) : this(
        channels.map { ChannelOutputModel(it) },
        channels.size,
    )
}
