import {MessagesOutputModel} from "./MessagesOutputModel";

export type GetMessagesListOutputModel = {
    messages: MessagesOutputModel[],
    size: number
}