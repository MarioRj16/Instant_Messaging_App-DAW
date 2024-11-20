package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.InviteRole
import java.sql.ResultSet
import org.jdbi.v3.core.mapper.RowMapper
import kotlinx.datetime.Instant
import org.jdbi.v3.core.statement.StatementContext

class ChannelInvitationMapper: RowMapper<ChannelInvitation> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext
    ): ChannelInvitation =
        ChannelInvitation(
            rs.getInt("channel_invitation_id"),
            rs.getInt("inviter_id"),
            rs.getInt("invitee_id"),
            rs.getInt("channel_id"),
            InviteRole.fromRole(rs.getString("role")),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
        )
}