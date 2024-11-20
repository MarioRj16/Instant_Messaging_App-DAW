package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Channel
import java.sql.ResultSet
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext

class ChannelMapper: RowMapper<Channel> {
    override fun map(rs: ResultSet, ctx: StatementContext): Channel {
        return Channel(
            rs.getInt("channel_id"),
            rs.getString("channel_name"),
            rs.getInt("owner_id"),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
            rs.getBoolean("is_public"),
            emptyList()
        )
    }

}