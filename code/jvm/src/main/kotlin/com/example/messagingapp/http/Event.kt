package com.example.messagingapp.http

sealed class Event {
    data class Message(val id: Int, val content: String) : Event()
    data class KeepAlive(val timestamp: kotlinx.datetime.Instant) : Event()
}