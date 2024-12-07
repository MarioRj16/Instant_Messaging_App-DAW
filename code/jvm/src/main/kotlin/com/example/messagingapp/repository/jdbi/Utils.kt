package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Membership
import com.example.messagingapp.domain.Message
import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.domain.User
import com.example.messagingapp.repository.jdbi.mappers.ChannelInvitationMapper
import com.example.messagingapp.repository.jdbi.mappers.ChannelMapper
import com.example.messagingapp.repository.jdbi.mappers.InstantMapper
import com.example.messagingapp.repository.jdbi.mappers.MembershipMapper
import com.example.messagingapp.repository.jdbi.mappers.MessageMapper
import com.example.messagingapp.repository.jdbi.mappers.PasswordMapper
import com.example.messagingapp.repository.jdbi.mappers.RegistrationInvitationMapper
import com.example.messagingapp.repository.jdbi.mappers.TokenMapper
import com.example.messagingapp.repository.jdbi.mappers.UserMapper
import com.example.messagingapp.utils.PaginatedResponse
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

data class ChannelResponse(val data: List<Channel>, val totalSize: Int)

class ChannelInvitationResponse(
    override val data: List<ChannelInvitation>,
    override val page: Int,
    override val totalPages: Int,
    override val totalSize: Int,
) : PaginatedResponse<ChannelInvitation>(data, page, totalPages, totalSize)

class MembershipResponse(
    override val data: List<Membership>,
    override val page: Int,
    override val totalPages: Int,
    override val totalSize: Int,
) : PaginatedResponse<Membership>(data, page, totalPages, totalSize)

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(Password::class.java, PasswordMapper())
    registerColumnMapper(Token::class.java, TokenMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())

    registerRowMapper(RegistrationInvitation::class.java, RegistrationInvitationMapper())
    registerRowMapper(ChannelInvitation::class.java, ChannelInvitationMapper())
    registerRowMapper(Message::class.java, MessageMapper())
    registerRowMapper(Membership::class.java, MembershipMapper())
    registerRowMapper(Channel::class.java, ChannelMapper())
    registerRowMapper(User::class.java, UserMapper())

    return this
}
