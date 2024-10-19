package com.example.messagingapp.domain

import java.util.UUID

class UUIDTokenEncoder : TokenEncoder {
    override fun createToken(): Token = Token(UUID.randomUUID())
}
