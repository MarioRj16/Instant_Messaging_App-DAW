package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.RegistrationInvitation
import com.example.messagingapp.domain.Token
import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.http.model.output.MembershipOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.repository.jdbi.mappers.ChannelInvitationOutputMapper
import com.example.messagingapp.repository.jdbi.mappers.ChannelWithMembershipMapper
import com.example.messagingapp.repository.jdbi.mappers.InstantMapper
import com.example.messagingapp.repository.jdbi.mappers.MembershipOutputMapper
import com.example.messagingapp.repository.jdbi.mappers.MessageOutputMapper
import com.example.messagingapp.repository.jdbi.mappers.PasswordMapper
import com.example.messagingapp.repository.jdbi.mappers.RegistrationInvitationMapper
import com.example.messagingapp.repository.jdbi.mappers.TokenMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(Password::class.java, PasswordMapper())
    registerColumnMapper(Token::class.java, TokenMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())

    registerRowMapper(RegistrationInvitation::class.java, RegistrationInvitationMapper())
    registerRowMapper(ChannelWithMembershipOutputModel::class.java, ChannelWithMembershipMapper())
    registerRowMapper(ChannelInvitationOutputModel::class.java, ChannelInvitationOutputMapper())
    registerRowMapper(MembershipOutputModel::class.java, MembershipOutputMapper())
    registerRowMapper(MessageOutputModel::class.java, MessageOutputMapper())
    return this
}
