package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InvitationNotFound(
    override val instance: URI
): Problem(
    type = INVITATION_NOT_FOUND,
    title = "Invitation not found",
    detail = "The invitation was not found.",
    instance = instance,
)
