package com.example.messagingapp.http.pipeline

import com.example.messagingapp.domain.AuthenticatedUser
import com.example.messagingapp.domain.Token
import com.example.messagingapp.services.UsersService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RequestTokenProcessor(
    val usersService: UsersService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != SCHEME) {
            return null
        }
        return usersService.getUserByToken(parts[1])?.let {
            AuthenticatedUser(
                it,
                Token(UUID.fromString(parts[1])),
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
