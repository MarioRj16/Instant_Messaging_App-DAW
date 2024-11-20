package com.example.messagingapp.http

interface EventEmitter {
    fun emit(event: Event)
    fun onCompletion(callback: () -> Unit)
    fun onError(callback: (Throwable) -> Unit)
}

