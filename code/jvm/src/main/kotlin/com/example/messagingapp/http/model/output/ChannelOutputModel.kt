package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel
import io.swagger.v3.oas.annotations.media.Schema

data class ChannelOutputModel(
    @Schema(description = "Channel ID", example = "1")
    val channelId: Int,
    @Schema(description = "Channel name", example = "General")
    val channelName: String,
    @Schema(description = "Channel owner")
    val owner: UserOutputModel,
    @Schema(description = "Channel creation date", example = "2021-08-01T12:00:00")
    val createdAt: String,
    @Schema(description = "Is channel public", example = "true")
    val isPublic: Boolean,
    @Schema(description = "Channel members")
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
