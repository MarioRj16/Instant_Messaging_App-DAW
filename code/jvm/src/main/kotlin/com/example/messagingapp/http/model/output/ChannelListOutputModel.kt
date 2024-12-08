package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.utils.PaginatedResponse
import io.swagger.v3.oas.annotations.media.Schema

class ChannelListOutputModel(
    @Schema(description = "List of channels")
    val channels: List<ChannelOutputModel>,
    @Schema(description = "Number of items per page", example = "10")
    val pageSize: Int,
    @Schema(description = "Current page number", example = "1")
    val page: Int,
    @Schema(description = "Total number of pages", example = "1")
    val totalPages: Int,
    @Schema(description = "Total number of items", example = "1")
    val totalSize: Int,
    @Schema(description = "Whether there is a previous page", example = "false")
    val hasPrevious: Boolean,
    @Schema(description = "Whether there is a next page", example = "false")
    val hasNext: Boolean,
    @Schema(description = "Previous page number", example = "1")
    val previousPage: Int?,
    @Schema(description = "Next page number", example = "1")
    val nextPage: Int?,
) {
    constructor(response: PaginatedResponse<Channel>) : this(
        response.data.map { ChannelOutputModel(it) },
        response.pageSize,
        response.page,
        response.totalPages,
        response.totalSize,
        response.hasPrevious,
        response.hasNext,
        response.previousPage,
        response.nextPage,
    )
}
