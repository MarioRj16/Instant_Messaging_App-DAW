package com.example.messagingapp.http.model.input

import io.swagger.v3.oas.annotations.media.Schema

data class InvitationInputModel(
    @Schema(description = "The username of the user to invite", example = "user")
    val username: String,
    @Schema(description = "The role to assign to the user in the channel", example = "MEMBER")
    val role: String,
)
