import {ChannelInvitationOutputModel} from "./ChannelInvitationOutputModel";

export type GetChannelInvitationsListOutputModel = {
    invitations: ChannelInvitationOutputModel[];
    size: number;
}