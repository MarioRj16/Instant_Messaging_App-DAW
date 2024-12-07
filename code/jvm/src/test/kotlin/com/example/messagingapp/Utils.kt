package com.example.messagingapp

import com.example.messagingapp.domain.AuthToken
import com.example.messagingapp.domain.ChannelDomain
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.repository.jdbi.JdbiTransactionManager
import com.example.messagingapp.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Generates a random string.
 *
 * @return The generated string.
 */
fun generateRandomString() = "user-${abs(Random.nextLong())}"

/**
 * Clears the database.
 *
 * @param jdbi The Jdbi instance to use for the database operations.
 */
fun clearDatabase(jdbi: Jdbi) {
    jdbi.useHandle<Exception> { handle ->
        handle.execute("DELETE FROM auth_token")
        handle.execute("DELETE FROM message")
        handle.execute("DELETE FROM membership")
        handle.execute("DELETE FROM channel_invitation")
        handle.execute("DELETE FROM channel")
        handle.execute("DELETE FROM users")
        handle.execute("DELETE FROM registration_invitation")

        handle.execute("ALTER SEQUENCE channel_channel_id_seq RESTART WITH 1")
        handle.execute("ALTER SEQUENCE channel_invitation_channel_invitation_id_seq RESTART WITH 1")
        handle.execute("ALTER SEQUENCE membership_membership_id_seq RESTART WITH 1")
        handle.execute("ALTER SEQUENCE message_message_id_seq RESTART WITH 1")
        handle.execute("ALTER SEQUENCE users_user_id_seq RESTART WITH 1")
    }
}

val jdbi =
    Jdbi
        .create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()

val transactionManager = JdbiTransactionManager(jdbi)

fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

val userDomainConfig =
    UserDomainConfig(
        tokenTTL = 30.days,
        tokenRollingTTL = 30.minutes,
        maxTokensPerUser = 3,
        registrationInvitationTTL = 24.hours,
        invitationCodeLength = 4,
    )

val userDomain =
    UserDomain(
        BCryptPasswordEncoder(),
        UUIDTokenEncoder(),
        userDomainConfig,
    )

/**
 * Creates a user in the database and returns its ID.
 *
 * @param username The username of the user.
 * @param password The password of the user.
 * @param testClock The clock to use for the creation of the user.
 */
fun bootstrapUser(
    username: String = generateRandomString(),
    password: String = "Password123@",
    testClock: Clock,
): Int {
    return transactionManager.run {
        val invitationCode = generateInvitationCode(testClock)
        return@run it.usersRepository.createUser(
            username,
            userDomain.hashPassword(password),
            invitationCode,
        )
    }
}

/**
 * Creates a channel in the database and returns its ID.
 *
 * @param ownerId The ID of the user who owns the channel.
 * @param channelName The name of the channel.
 * @param isPublic Whether the channel is public.
 * @param clock The clock to use for the creation of the channel.
 *
 * @return The ID of the created channel.
 */
fun bootstrapChannel(
    ownerId: Int,
    channelName: String = generateRandomString(),
    isPublic: Boolean = true,
    clock: Clock = TestClock(),
): Int {
    return transactionManager.run {
        val channelId =
            it.channelsRepository.createChannel(
                channelName,
                isPublic,
                ownerId,
                clock,
            )
        it.channelsRepository.createMembership(ownerId, channelId, clock, MembershipRole.OWNER.role)
        return@run channelId
    }
}

val channelDomain = ChannelDomain()

/**
 * Generates a random invitation code and stores it in the database.
 *
 * @param clock The clock to use for the creation of the invitation code.
 * @return The generated invitation code.
 */
fun generateInvitationCode(clock: Clock): String {
    var invitationCode = userDomain.createInvitationCode()
    transactionManager.run {
        while (it.usersRepository.getRegistrationInvitation(invitationCode) != null) {
            invitationCode = userDomain.createInvitationCode()
        }
        it.usersRepository.createRegistrationInvitation(clock, invitationCode)
    }
    return invitationCode
}

/**
 * Generates a token for a user and stores it in the database.
 *
 * @param userId The ID of the user for whom to generate the token.
 * @param clock The clock to use for the creation of the token.
 *
 * @return The generated token.
 */
fun generateToken(
    userId: Int,
    clock: Clock,
): UUID {
    return transactionManager.run {
        val now = clock.now()
        val token = AuthToken(userDomain.createToken(), userId, now, now)
        it.usersRepository.createToken(token, userDomainConfig.maxTokensPerUser)
        return@run token.token.value
    }
}
