package com.example.messagingapp.repository

interface Transaction {
    val usersRepository: UsersRepository
    val channelsRepository: ChannelsRepository

    fun commit()

    fun rollback()
}
