package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UserNotInChannel(
    override val instance: URI,
): Problem(
    USER_NOT_IN_CHANNEL,
    "User not in channel",
    "The user is not a member of channel.",
    instance
)
