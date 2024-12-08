package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.User
import io.swagger.v3.oas.annotations.media.Schema

data class UserOutputModel(
    @Schema(description = "User ID", example = "1")
    val userId: Int,
    @Schema(description = "Username", example = "user")
    val username: String,
) {
    constructor(user: User) : this(
        user.userId,
        user.username,
    )
}
