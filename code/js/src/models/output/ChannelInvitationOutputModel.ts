import {UserOutputModel} from "./UserOutputModel";
import {ChannelOutputModel} from "./ChannelOutputModel";

export type ChannelInvitationOutputModel = {
    channelInvitationId: number,
    inviter: UserOutputModel,
    inviteeId: number,
    channel: ChannelOutputModel,
    role: string,
    createdAt: string,
}
/*
    val channelInvitationId: Int,
    val inviter: UserOutputModel,
    val inviteeId: Int,
    val channel: ChannelOutputModel,
    val role: String,
    val createdAt: String,
 */