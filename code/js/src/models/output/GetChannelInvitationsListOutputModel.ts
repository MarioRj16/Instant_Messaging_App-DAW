import {ChannelInvitationOutputModel} from "./ChannelInvitationOutputModel";

export type GetChannelInvitationsListOutputModel = {
    invitations: ChannelInvitationOutputModel[];
    pageSize: number;
    page: number;
    totalPages: number;
    totalSize: number;
    hasPrevious: boolean;
    hasNext: boolean;
    previousPage: number | null;
    nextPage: number | null;
}
/*
    val invitations: List<ChannelInvitationOutputModel>,
    val pageSize: Int,
    val page: Int,
    val totalPages: Int,
    val totalSize: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val previousPage: Int?,
    val nextPage: Int?,
 */