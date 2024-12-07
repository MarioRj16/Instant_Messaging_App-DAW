package com.example.messagingapp.utils

open class PaginatedResponse<T>(
    open val data: List<T>,
    val pageSize: Int,
    open val page: Int,
    open val totalPages: Int,
    open val totalSize: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val previousPage: Int?,
    val nextPage: Int?,
) {
    constructor(data: List<T>, page: Int, totalPages: Int, totalSize: Int) : this(
        data = data,
        pageSize = data.size,
        page = page,
        totalPages = totalPages,
        totalSize = totalSize,
        hasPrevious = page > 1,
        hasNext = page < totalPages,
        previousPage = if (page > 1) page - 1 else null,
        nextPage = if (page < totalPages) page + 1 else null,
    )

    open fun copy(
        data: List<T> = this.data,
        page: Int = this.page,
        totalPages: Int = this.totalPages,
        totalSize: Int = this.totalSize,
    ): PaginatedResponse<T> = PaginatedResponse(data, page, totalPages, totalSize)
}
