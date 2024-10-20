package com.example.messagingapp.http.model.output

class ChannelListOutputModel(
    val channels: List<ChannelWithMembershipOutputModel>,
    val size: Int,
) {
    constructor(channels: List<ChannelWithMembershipOutputModel>) : this(
        channels,
        channels.size,
    )
}
