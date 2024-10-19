package com.example.messagingapp.repository

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import kotlinx.datetime.Instant

interface UsersRepository {
    fun getUserById(userId: Int): User?

    fun getUserByUsername(username: String): User?

    fun getUserByEmail(email: String): User?

    fun getUserByToken(token: Token): AuthToken?

    fun createUser(
        username: String,
        email: String,
        password: Password,
    ): Int

    fun getToken(userId: Int): AuthToken?

    fun createToken(
        authToken: AuthToken,
        maxTokens: Int,
    )

    fun updateToken(token: Token)

    fun deleteToken(token: Token): Boolean

    fun createRegistrationInvitation(
        inviterId: Int,
        createdAt: Instant,
    ): Token

    fun getRegistrationInvitation(token: Token): RegistrationInvitation?

    fun acceptRegistrationInvitation(token: Token)

    fun declineRegistrationInvitation(token: Token)
}
