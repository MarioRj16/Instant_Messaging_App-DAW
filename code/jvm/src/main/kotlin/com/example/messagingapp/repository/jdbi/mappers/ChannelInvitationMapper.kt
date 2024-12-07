package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.InviteRole
import com.example.messagingapp.domain.User
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelInvitationMapper : RowMapper<ChannelInvitation> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): ChannelInvitation =
        ChannelInvitation(
            rs.getInt("channel_invitation_id"),
            ctx.findRowMapperFor(User::class.java).get().map(rs, ctx),
            rs.getInt("invitee_id"),
            ctx.findRowMapperFor(Channel::class.java).get().map(rs, ctx),
            InviteRole.fromRole(rs.getString("role")),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
        )
}
