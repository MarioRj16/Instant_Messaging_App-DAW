package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import com.example.messagingapp.repository.UsersRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import java.util.UUID

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    override fun getUserById(userId: Int): User? =
        handle
            .createQuery("SELECT * FROM users WHERE user_id = :id")
            .bind("id", userId)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserByUsername(username: String): User? =
        handle
            .createQuery("SELECT * FROM users WHERE username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserByEmail(email: String): User? =
        handle
            .createQuery("SELECT * FROM users WHERE email = :email")
            .bind("email", email)
            .mapTo<User>()
            .singleOrNull()

    override fun getUserByToken(token: Token): AuthToken? =
        handle
            .createQuery("""SELECT * FROM auth_token WHERE token = :token""")
            .bind("token", token.value)
            .mapTo<AuthToken>()
            .singleOrNull()

    override fun createUser(
        username: String,
        email: String,
        password: Password,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO users (username, email, password)
                VALUES (:username, :email, :password)
                """,
            ).bind("username", username)
            .bind("email", email)
            .bind("password", password.value)
            .executeAndReturnGeneratedKeys("user_id")
            .mapTo<Int>()
            .one()

    override fun getToken(userId: Int): AuthToken? =
        handle
            .createQuery("SELECT * FROM auth_token WHERE user_id = :userId")
            .bind("userId", userId)
            .mapTo<AuthToken>()
            .singleOrNull()

    override fun createToken(
        authToken: AuthToken,
        maxTokens: Int,
    ) {
        val deletions =
            handle.createUpdate(
                """
                DELETE FROM auth_token 
                WHERE user_id = :user_id 
                    AND auth_token.token IN (
                        SELECT token FROM auth_token WHERE user_id = :user_id 
                            ORDER BY last_used_at DESC offset :offset
                    )
                """.trimIndent(),
            )
                .bind("user_id", authToken.userId)
                .bind("offset", maxTokens - 1)
                .execute()

        logger.info("Deleted $deletions tokens for user ${authToken.userId}")

        handle.createUpdate(
            """
            INSERT INTO auth_token (user_id, token, created_at, last_used_at) 
            VALUES (:userId, :token, :created_at, :last_used_at)
            """.trimIndent(),
        )
            .bind("userId", authToken.userId)
            .bind("token", authToken.token.value)
            .bind("created_at", authToken.createdAt.epochSeconds)
            .bind("last_used_at", authToken.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateToken(token: Token) {
        handle
            .createUpdate("UPDATE auth_token SET last_used_at = :current_time WHERE token = :token")
            .bind("current_time", Clock.System.now().epochSeconds)
            .bind("token", token.value)
            .execute()
    }

    override fun deleteToken(token: Token): Boolean =
        handle
            .createUpdate("DELETE FROM auth_token WHERE token = :token")
            .bind("token", token.value)
            .execute()
            .let { it > 0 }

    override fun createRegistrationInvitation(
        inviterId: Int,
        createdAt: Instant,
    ): Token {
        val token = UUID.randomUUID()
        handle
            .createUpdate(
                """
                INSERT INTO registration_invitation (invitation_token, inviter_id, created_at, status)
                VALUES (:token, :inviterId, :createdAt, 'pending')
                """,
            ).bind("token", token)
            .bind("inviterId", inviterId)
            .bind("createdAt", createdAt.epochSeconds)
            .execute()
        return Token(token)
    }

    override fun getRegistrationInvitation(token: Token): RegistrationInvitation? =
        handle
            .createQuery("""SELECT * FROM registration_invitation WHERE invitation_token = :token""")
            .bind("token", token.value)
            .mapTo<RegistrationInvitation>()
            .singleOrNull()

    override fun acceptRegistrationInvitation(token: Token) {
        handle
            .createUpdate(
                """
                UPDATE registration_invitation
                SET status = 'accepted'
                WHERE invitation_token = :token
                """,
            ).bind("token", token.value)
            .execute()
    }

    override fun declineRegistrationInvitation(token: Token) {
        handle
            .createUpdate(
                """
                UPDATE registration_invitation
                SET status = 'rejected'
                WHERE invitation_token = :token
                """,
            ).bind("token", token.value)
            .execute()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
