package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.repository.ChannelsRepository
import com.example.messagingapp.repository.Transaction
import com.example.messagingapp.repository.UsersRepository
import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository: UsersRepository = JdbiUsersRepository(handle)

    override val channelsRepository: ChannelsRepository = JdbiChannelsRepository(handle)

    override fun commit() {
        handle.commit()
    }

    override fun rollback() {
        handle.rollback()
    }
}
