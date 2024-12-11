package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InvitationCodeNotValid(
    val invitationCode: String,
    override val instance: URI,
): Problem(
    INVITATION_CODE_NOT_VALID,
    "Invitation code not valid",
    "The invitation code $invitationCode is not valid",
    instance
)