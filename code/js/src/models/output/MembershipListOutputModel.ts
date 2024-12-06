import {MembershipOutputModel} from "./MembershipOutputModel";

export type MembershipListOutputModel = {
    memberships: MembershipOutputModel[];
    size: number;

    /*
    val memberships: List<MembershipOutputModel>,
    val size: Int,

     */
}