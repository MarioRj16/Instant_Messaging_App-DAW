package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.http.model.MessageOutput
import com.example.messagingapp.http.model.SenderInfoOutput
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class MessageOutputMapper : RowMapper<MessageOutput> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): MessageOutput {
        return MessageOutput(
            rs.getInt("message_id"),
            SenderInfoOutput(rs.getInt("sender_id"), rs.getString("sender_name")),
            rs.getInt("channel_id"),
            rs.getString("content"),
            rs.getLong("created_at"),
        )
    }
}
