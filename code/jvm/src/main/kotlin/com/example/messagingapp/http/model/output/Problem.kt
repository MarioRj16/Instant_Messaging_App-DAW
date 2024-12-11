package com.example.messagingapp.http.model.output

import org.springframework.http.ResponseEntity
import java.net.URI

abstract class Problem(
    val type: URI,
    open val title: String,
    open val detail: String? = null,
    open val instance: URI,
) {
    companion object {
        private const val BASE_URI = "https://github.com/isel-leic-daw/2024-daw-leic51d-g04-1/docs/problems"
        private const val MEDIA_TYPE = "application/problem+json"
        private const val LANGUAGE = "en"

        fun response(
            status: Int,
            problem: Problem,
        ) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .header("Content-Language", LANGUAGE)
            .body<Any>(problem)

        val INVALID_PAGINATION = URI("$BASE_URI/invalid-pagination")

        // User problems
        val INVITATION_CODE_NOT_VALID = URI("$BASE_URI/invitation-code-not-valid")
        val USERNAME_ALREADY_EXISTS = URI("$BASE_URI/username-already-exists")
        val USERNAME_NOT_VALID = URI("$BASE_URI/username-not-valid")
        val PASSWORD_NOT_SAFE = URI("$BASE_URI/password-not-safe")
        val USER_OR_PASSWORD_INVALID = URI("$BASE_URI/user-or-password-invalid")

        // Channel problems
        val INVALID_CHANNEL_NAME = URI("$BASE_URI/invalid-channel-name")
        val CHANNEL_NAME_ALREADY_EXISTS = URI("$BASE_URI/channel-name-already-exists")
        val CHANNEL_NOT_FOUND = URI("$BASE_URI/channel-not-found")
        val USER_ALREADY_IN_CHANNEL = URI("$BASE_URI/user-already-in-channel")
        val CHANNEL_NOT_PUBLIC = URI("$BASE_URI/channel-not-public")
        val USER_NOT_IN_CHANNEL = URI("$BASE_URI/user-not-in-channel")
        val USER_NOT_AUTHORIZED_TO_WRITE = URI("$BASE_URI/user-not-authorized-to-write")
        val INVITEE_NOT_FOUND = URI("$BASE_URI/invitee-not-found")
        val INVITATION_ALREADY_EXISTS = URI("$BASE_URI/invitation-already-exists")
        val FORBIDDEN_ROLE = URI("$BASE_URI/forbidden-role")
        val INVITATION_NOT_FOUND = URI("$BASE_URI/invitation-not-found")
        val USER_IS_OWNER = URI("$BASE_URI/user-is-owner")

        val INTERNAL_SERVER_ERROR = URI("$BASE_URI/internal-server-error")
    }
}
