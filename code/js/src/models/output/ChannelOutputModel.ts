import {UserOutputModel} from "./UserOutputModel";
import {MembershipListOutputModel} from "./MembershipListOutputModel";

export type ChannelOutputModel = {
    channelId: number,
    channelName: string,
    owner: UserOutputModel,
    createdAt: string,
    isPublic: boolean,
    members: MembershipListOutputModel
}