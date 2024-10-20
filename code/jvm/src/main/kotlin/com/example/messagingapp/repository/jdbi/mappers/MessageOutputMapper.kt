package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.http.model.output.SenderInfoOutputModel
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class MessageOutputMapper : RowMapper<MessageOutputModel> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): MessageOutputModel {
        return MessageOutputModel(
            rs.getInt("message_id"),
            SenderInfoOutputModel(rs.getInt("sender_id"), rs.getString("sender_name")),
            rs.getInt("channel_id"),
            rs.getString("content"),
            Instant.fromEpochSeconds(rs.getLong("created_at")).toString(),
        )
    }
}
