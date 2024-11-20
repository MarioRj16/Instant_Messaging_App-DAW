package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Message
import com.example.messagingapp.domain.User
import java.sql.ResultSet
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext

class MessageMapper: RowMapper<Message> {
    override fun map(rs: ResultSet, ctx: StatementContext): Message {
        return Message(
            rs.getInt("message_id"),
            rs.getInt("channel_id"),
            ctx.findRowMapperFor(User::class.java).get().map(rs, ctx),
            rs.getString("content"),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
        )
    }
}