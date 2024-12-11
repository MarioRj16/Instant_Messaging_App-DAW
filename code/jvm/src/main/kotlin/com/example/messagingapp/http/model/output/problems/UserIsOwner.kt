package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UserIsOwner(
    override val instance: URI
): Problem(
    type = USER_IS_OWNER,
    title = "User is owner",
    detail = "The user is the owner of the channel.",
    instance = instance,
)
