package com.example.messagingapp.domain

interface TokenEncoder {
    fun createToken(): Token
}
