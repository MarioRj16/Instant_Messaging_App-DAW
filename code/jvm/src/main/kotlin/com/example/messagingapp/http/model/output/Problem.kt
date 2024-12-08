package com.example.messagingapp.http.model.output

import org.springframework.http.ResponseEntity
import java.net.URI

data class Problem(

    val type: URI,
    val title: String,
    val detail: String? = null,
    val instance: URI,
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
        private val INVITATION_CODE_NOT_VALID = URI("$BASE_URI/invitation-code-not-valid")
        private val USERNAME_ALREADY_EXISTS = URI("$BASE_URI/username-already-exists")
        private val USERNAME_NOT_VALID = URI("$BASE_URI/username-not-valid")
        private val PASSWORD_NOT_SAFE = URI("$BASE_URI/password-not-safe")
        private val USER_OR_PASSWORD_INVALID = URI("$BASE_URI/user-or-password-invalid")

        // Channel problems
        private val INVALID_CHANNEL_NAME = URI("$BASE_URI/invalid-channel-name")
        private val CHANNEL_NAME_ALREADY_EXISTS = URI("$BASE_URI/channel-name-already-exists")
        private val CHANNEL_NOT_FOUND = URI("$BASE_URI/channel-not-found")
        private val USER_ALREADY_IN_CHANNEL = URI("$BASE_URI/user-already-in-channel")
        private val CHANNEL_NOT_PUBLIC = URI("$BASE_URI/channel-not-public")
        private val USER_NOT_IN_CHANNEL = URI("$BASE_URI/user-not-in-channel")
        private val USER_NOT_AUTHORIZED_TO_WRITE = URI("$BASE_URI/user-not-authorized-to-write")
        private val INVITEE_NOT_FOUND = URI("$BASE_URI/invitee-not-found")
        private val MEMBERSHIP_NOT_FOUND = URI("$BASE_URI/membership-not-found")
        private val FORBIDDEN_ROLE = URI("$BASE_URI/forbidden-role")
        private val INVITATION_NOT_FOUND = URI("$BASE_URI/invitation-not-found")
        private val USER_IS_OWNER = URI("$BASE_URI/user-is-owner")

        private val INTERNAL_SERVER_ERROR = URI("$BASE_URI/internal-server-error")

        fun invalidPagination(instance: URI) =
            Problem(
                INVALID_PAGINATION,
                "Invalid pagination",
                "The pagination parameters are invalid. Both must be positive integers.",
                instance,
            )

        fun invitationCodeNotValid(
            invitationCode: String,
            instance: URI,
        ) = Problem(
            INVITATION_CODE_NOT_VALID,
            "Invitation code not valid",
            "The invitation code $invitationCode is not valid",
            instance,
        )

        fun usernameAlreadyExists(
            username: String,
            instance: URI,
        ) = Problem(
            USERNAME_ALREADY_EXISTS,
            "Username already exists",
            "The username $username already exists. Please choose another one.",
            instance,
        )

        fun usernameNotValid(
            username: String,
            instance: URI,
        ) = Problem(
            USERNAME_NOT_VALID,
            "Username not valid",
            """
                |The username $username is not valid. 
                |The Username must be between 3 and 64 characters long and contain only letters, numbers, and 
                |underscores.
                |
            """.trimMargin(),
            instance,
        )

        fun passwordNotSafe(instance: URI) =
            Problem(
                PASSWORD_NOT_SAFE,
                "Password not safe",
                """
                The password must be at least 8 characters long 
                and contain at least one uppercase letter, 
                one lowercase letter, 
                one number, 
                and one special character.
                """.trimIndent(),
                instance,
            )

        fun userOrPasswordInvalid(instance: URI) =
            Problem(
                USER_OR_PASSWORD_INVALID,
                "User or password invalid",
                "The user or password is invalid",
                instance,
            )

        fun invalidChannelName(instance: URI) =
            Problem(
                INVALID_CHANNEL_NAME,
                "Invalid channel name",
                """A valid channel name must be between 3 and 64 characters long and contain only letters, numbers, and underscores.""",
                instance,
            )

        fun channelNameAlreadyExists(
            channelName: String,
            instance: URI,
        ) = Problem(
            CHANNEL_NAME_ALREADY_EXISTS,
            "Channel name already exists",
            "The channel name $channelName already exists. Please choose another one.",
            instance,
        )

        fun channelNotFound(
            channelId: Int,
            instance: URI,
        ) = Problem(
            CHANNEL_NOT_FOUND,
            "Channel not found",
            "The channel $channelId was not found",
            instance,
        )

        fun userAlreadyInChannel(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            USER_ALREADY_IN_CHANNEL,
            "User already in channel",
            "The user $userId is already in the channel $channelId",
            instance,
        )

        fun channelNotPublic(
            channelId: Int,
            instance: URI,
        ) = Problem(
            CHANNEL_NOT_PUBLIC,
            "Channel not public",
            "The channel $channelId is not public",
            instance,
        )

        fun userNotInChannel(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            USER_NOT_IN_CHANNEL,
            "User not in channel",
            "The user $userId is not in the channel $channelId",
            instance,
        )

        fun userNotAuthorizedToWrite(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            USER_NOT_AUTHORIZED_TO_WRITE,
            "User not authorized to write",
            "The user $userId is not authorized to write in the channel $channelId",
            instance,
        )

        fun inviteeNotFound(
            inviteeUsername: String,
            instance: URI,
        ) = Problem(
            INVITEE_NOT_FOUND,
            "Invitee not found",
            "The invitee $inviteeUsername was not found",
            instance,
        )

        fun membershipNotFound(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            MEMBERSHIP_NOT_FOUND,
            "Membership not found",
            "The membership of the user $userId in the channel $channelId was not found",
            instance,
        )

        fun forbiddenRole(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            FORBIDDEN_ROLE,
            "Forbidden role",
            "The user $userId has a forbidden role in the channel $channelId",
            instance,
        )

        fun invitationNotFound(
            invitationId: Int,
            instance: URI,
        ) = Problem(
            INVITATION_NOT_FOUND,
            "Invitation not found",
            "The invitation $invitationId was not found",
            instance,
        )

        fun userIsOwner(
            userId: Int,
            channelId: Int,
            instance: URI,
        ) = Problem(
            USER_IS_OWNER,
            "User is owner",
            "The user $userId is the owner of the channel $channelId",
            instance,
        )

        fun internalServerError(
            detail: String,
            instance: URI,
        ) = Problem(
            INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            detail,
            instance,
        )
    }
}
