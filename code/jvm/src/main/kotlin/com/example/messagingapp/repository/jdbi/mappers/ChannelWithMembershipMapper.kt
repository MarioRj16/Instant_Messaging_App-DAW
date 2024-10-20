package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelWithMembershipMapper : RowMapper<ChannelWithMembershipOutputModel> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): ChannelWithMembershipOutputModel {
        return ChannelWithMembershipOutputModel(
            rs.getInt("channel_id"),
            rs.getString("channel_name"),
            rs.getBoolean("is_public"),
            rs.getInt("owner_id"),
            rs.getBoolean("is_member"),
            Instant.fromEpochSeconds(rs.getLong("created_at")).toString(),
        )
    }
}
