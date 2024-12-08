package com.example.messagingapp.http.model.output

import io.swagger.v3.oas.annotations.media.Schema

data class RegistrationInvitationCreateOutputModel(
    @Schema(description = "Invitation code", example = "C0D3")
    val invitationCode: String
)
