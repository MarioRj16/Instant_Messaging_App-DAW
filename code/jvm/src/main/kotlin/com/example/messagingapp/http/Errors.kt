package com.example.messagingapp.http

import com.example.messagingapp.http.model.output.Problem
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class InvalidPaginationException(message: String) : RuntimeException(message)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(InvalidPaginationException::class)
    fun handleInvalidPagination(exception: InvalidPaginationException): ResponseEntity<*> {
        return Problem.response(
            HttpStatus.BAD_REQUEST.value(),
            Problem.invalidPagination(Problem.INVALID_PAGINATION),
        )
    }
}
