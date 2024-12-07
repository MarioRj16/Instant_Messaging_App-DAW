package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.utils.PaginatedResponse

class ChannelListOutputModel(
    val channels: List<ChannelOutputModel>,
    val pageSize: Int,
    val page: Int,
    val totalPages: Int,
    val totalSize: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val previousPage: Int?,
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
