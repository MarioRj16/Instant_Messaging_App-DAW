package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UsernameAlreadyExists(
    val username: String,
    override val instance: URI,
) : Problem(
    type = USERNAME_ALREADY_EXISTS,
    title = "Username already exists",
    detail = "The username '$username' already exists. Please choose another username.",
    instance = instance,
)
