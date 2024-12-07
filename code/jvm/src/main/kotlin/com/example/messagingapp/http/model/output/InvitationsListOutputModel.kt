package com.example.messagingapp.http.model.output

import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.utils.PaginatedResponse

class InvitationsListOutputModel(
    val invitations: List<ChannelInvitationOutputModel>,
    val pageSize: Int,
    val page: Int,
    val totalPages: Int,
    val totalSize: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val previousPage: Int?,
    val nextPage: Int?,
) {
    constructor(response: PaginatedResponse<ChannelInvitation>) : this(
        response.data.map { ChannelInvitationOutputModel(it) },
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
