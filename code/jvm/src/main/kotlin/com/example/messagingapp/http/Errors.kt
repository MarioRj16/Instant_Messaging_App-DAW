package com.example.messagingapp.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class InvalidPaginationException(message: String) : RuntimeException(message)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(InvalidPaginationException::class)
    fun handleInvalidPagination(exception: InvalidPaginationException): ResponseEntity<Map<String, String>> {
        return ResponseEntity(
            mapOf("error" to exception.message.orEmpty()),
            HttpStatus.BAD_REQUEST,
        )
    }
}
