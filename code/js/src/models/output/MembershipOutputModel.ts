import {UserOutputModel} from "./UserOutputModel";

export type MembershipOutputModel = {
    membershipId: number;
    user: UserOutputModel;
    channelId: number;
    role: string;
    joinedAt: string;
};
/*
data class MembershipOutputModel(
    val membershipId: Int,
    val user: UserOutputModel,
    val channelId: Int,
    val role: String,
    val joinedAt: String,
) {
 */