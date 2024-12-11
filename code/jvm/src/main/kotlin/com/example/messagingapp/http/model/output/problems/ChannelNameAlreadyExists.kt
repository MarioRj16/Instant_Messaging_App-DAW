package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class ChannelNameAlreadyExists(
    val channelName: String,
    override val instance: URI,
) : Problem(
    type = CHANNEL_NAME_ALREADY_EXISTS,
    title = "Channel name already exists",
    detail = "The channel name '$channelName' already exists. Please choose another channel name.",
    instance = instance,
)
