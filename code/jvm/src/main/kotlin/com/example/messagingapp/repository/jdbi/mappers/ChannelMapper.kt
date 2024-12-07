package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.User
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class ChannelMapper : RowMapper<Channel> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Channel {
        return Channel(
            rs.getInt("channel_id"),
            rs.getString("channel_name"),
            ctx.findRowMapperFor(User::class.java).get().map(rs, ctx),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
            rs.getBoolean("is_public"),
            emptyList(),
        )
    }
}
