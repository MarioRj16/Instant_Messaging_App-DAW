import {UserOutputModel} from "./UserOutputModel";

export type MembershipOutputModel = {
    membershipId: number;
    user: UserOutputModel;
    channelId: number;
    role: string;
    joinedAt: string;
};
