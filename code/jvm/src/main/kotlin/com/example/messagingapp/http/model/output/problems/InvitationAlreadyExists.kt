package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InvitationAlreadyExists(
    override val instance: URI
): Problem(
    type = INVITATION_ALREADY_EXISTS,
    title = "Invitation already exists",
    detail = "The user has already been invited to the channel",
    instance = instance
)
