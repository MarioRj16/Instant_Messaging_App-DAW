package com.example.messagingapp.http.model.output

import com.example.messagingapp.services.TokenExternalData
import io.swagger.v3.oas.annotations.media.Schema

data class LoginOutputModel(
    @Schema(description = "Token")
    val token: String,
    @Schema(description = "Token expiration date", example = "2021-08-01T00:00:00Z")
    val expiration: String,
) {
    constructor(tokenExternalData: TokenExternalData) : this(
        token = tokenExternalData.token.value.toString(),
        expiration = tokenExternalData.expiration.toString(),
    )
}
