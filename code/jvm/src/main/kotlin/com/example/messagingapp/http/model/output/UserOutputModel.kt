package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.User

data class UserOutputModel(
    val userId: Int,
    val username: String,
){
    constructor(user: User) : this(
        user.userId,
        user.username,
    )
}
