package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.http.model.ChannelWithMembership
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelWithMembershipMapper : RowMapper<ChannelWithMembership> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): ChannelWithMembership {
        return ChannelWithMembership(
            rs.getInt("channel_id"),
            rs.getString("channel_name"),
            rs.getBoolean("is_public"),
            rs.getInt("owner_id"),
            rs.getBoolean("is_member"),
            Instant.fromEpochSeconds(rs.getLong("created_at")).toString(),
        )
    }
}
