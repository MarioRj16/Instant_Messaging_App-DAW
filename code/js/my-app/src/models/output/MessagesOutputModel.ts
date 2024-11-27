import {UserOutputModel} from "./UserOutputModel";

export type MessagesOutputModel = {
    messageId: number,
    senderInfo: UserOutputModel,
    channelId: number,
    content: string,
    createdAt: string
}