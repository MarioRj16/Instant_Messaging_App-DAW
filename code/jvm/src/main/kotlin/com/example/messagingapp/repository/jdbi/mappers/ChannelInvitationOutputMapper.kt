package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.InviteRole
import com.example.messagingapp.domain.InviteStatus
import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelInvitationOutputMapper : RowMapper<ChannelInvitationOutputModel> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): ChannelInvitationOutputModel {
        return ChannelInvitationOutputModel(
            rs.getInt("channel_invitation_id"),
            rs.getInt("inviter_id"),
            rs.getInt("invitee_id"),
            rs.getInt("channel_id"),
            InviteRole.fromRole(rs.getString("role")),
            rs.getLong("created_at"),
            rs.getLong("expires_at"),
            InviteStatus.fromValue(rs.getString("status")),
        )
    }
}
