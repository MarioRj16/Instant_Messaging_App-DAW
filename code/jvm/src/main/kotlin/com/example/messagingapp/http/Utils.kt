package com.example.messagingapp.http

import com.example.messagingapp.DEFAULT_PAGE

fun validatePaginationParams(
    page: Int,
    pageSize: Int,
) {
    if (page < DEFAULT_PAGE.toInt()) throw InvalidPaginationException("Page number must be non-negative.")
    if (pageSize <= 0) throw InvalidPaginationException("Page size must be positive.")
}
