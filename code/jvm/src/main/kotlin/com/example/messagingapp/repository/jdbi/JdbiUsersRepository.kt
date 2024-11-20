package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import com.example.messagingapp.repository.UsersRepository
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import java.util.UUID

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    override fun getUser(userId: Int): User? =
        handle
            .createQuery("SELECT * FROM users WHERE user_id = :id")
            .bind("id", userId)
            .mapTo<User>()
            .singleOrNull()

    override fun getUser(username: String): User? =
        handle
            .createQuery("SELECT * FROM users WHERE username = :username")
            .bind("username", username)
            .mapTo<User>()
            .singleOrNull()

    override fun getAuthToken(token: Token): AuthToken? =
        handle
            .createQuery("""SELECT * FROM auth_token WHERE token = :token""")
            .bind("token", token.value)
            .mapTo<AuthToken>()
            .singleOrNull()

    override fun createUser(
        username: String,
        password: Password,
        invitationCode: String,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO users (username, password, invitation_code)
                VALUES (:username, :password, :invitation_code)
                """,
            )
            .bind("username", username)
            .bind("password", password.value)
            .bind("invitation_code", invitationCode)
            .executeAndReturnGeneratedKeys("user_id")
            .mapTo<Int>()
            .one()

    override fun getAuthToken(userId: Int): AuthToken? =
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
        clock: Clock,
        invitationCode: String,
    ) {
        handle
            .createUpdate(
                """
                INSERT INTO registration_invitation (invitation_code, created_at)
                VALUES (:code, :createdAt)
                """,
            ).bind("code", invitationCode)
            .bind("createdAt", clock.now().epochSeconds)
            .execute()
    }

    override fun getRegistrationInvitation(invitationCode: String): RegistrationInvitation? =
        handle
            .createQuery("""SELECT * FROM registration_invitation WHERE invitation_code = :code""")
            .bind("code", invitationCode)
            .mapTo<RegistrationInvitation>()
            .singleOrNull()

    override fun registrationInvitationIsUsed(invitationCode: String): Boolean =
        handle
            .createQuery("""SELECT 1 FROM users WHERE invitation_code = :code""")
            .bind("code", invitationCode)
            .mapTo<Int>()
            .singleOrNull() != null

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
