package com.example.messagingapp.repository

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import kotlinx.datetime.Clock

interface UsersRepository {
    fun getUser(userId: Int): User?

    fun getUser(username: String): User?

    fun getAuthToken(token: Token): AuthToken?

    fun createUser(
        username: String,
        password: Password,
        invitationCode: String,
    ): Int

    fun getAuthToken(userId: Int): AuthToken?

    fun createToken(
        authToken: AuthToken,
        maxTokens: Int,
    )

    fun updateToken(token: Token)

    fun deleteToken(token: Token): Boolean

    fun createRegistrationInvitation(
        clock: Clock,
        invitationCode: String,
    )

    fun getRegistrationInvitation(invitationCode: String): RegistrationInvitation?

    fun registrationInvitationIsUsed(invitationCode: String): Boolean
}
