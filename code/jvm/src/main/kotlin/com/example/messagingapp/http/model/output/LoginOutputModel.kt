package com.example.messagingapp.http.model.output

import com.example.messagingapp.services.TokenExternalData

data class LoginOutputModel(
    val token: String,
    val expiration: String,
) {
    constructor(tokenExternalData: TokenExternalData) : this(
        token = tokenExternalData.token.value.toString(),
        expiration = tokenExternalData.expiration.toString(),
    )
}
