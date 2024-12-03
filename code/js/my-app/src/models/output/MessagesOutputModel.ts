import {UserOutputModel} from "./UserOutputModel";

export type MessageOutputModel = {
    messageId: number,
    senderInfo: UserOutputModel,
    channelId: number,
    content: string,
    createdAt: string
}