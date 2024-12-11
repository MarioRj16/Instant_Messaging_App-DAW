package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InviteeNotFound(
    override val instance: URI
) : Problem(
    type = INVITEE_NOT_FOUND,
    title = "Invitee not found",
    detail = "The invitee was not found.",
    instance = instance,
)
