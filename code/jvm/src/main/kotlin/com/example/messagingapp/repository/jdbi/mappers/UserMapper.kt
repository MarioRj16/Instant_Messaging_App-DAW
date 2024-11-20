package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Password
import com.example.messagingapp.domain.User
import java.sql.ResultSet
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext

class UserMapper: RowMapper<User> {
    override fun map(rs: ResultSet, ctx: StatementContext): User {
        return User(
            userId = rs.getInt("user_id"),
            username = rs.getString("username"),
            password = ctx.findColumnMapperFor(Password::class.java).get().map(rs, "password", ctx),
            invitationCode = rs.getString("invitation_code"),
        )
    }
}