package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel

data class ChannelOutputModel(
    val channelId: Int,
    val channelName: String,
    val owner: UserOutputModel,
    val createdAt: String,
    val isPublic: Boolean,
    val members: MembershipListOutputModel,
) {
    constructor(channel: Channel) : this(
        channel.channelId,
        channel.channelName,
        UserOutputModel(channel.owner),
        channel.createdAt.toString(),
        channel.isPublic,
        MembershipListOutputModel(channel.members),
    )
}
