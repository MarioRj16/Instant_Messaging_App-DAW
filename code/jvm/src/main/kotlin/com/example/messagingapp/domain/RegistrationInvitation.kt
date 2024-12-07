package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class RegistrationInvitation(
    val invitationCode: String,
    val createdAt: Instant,
)
