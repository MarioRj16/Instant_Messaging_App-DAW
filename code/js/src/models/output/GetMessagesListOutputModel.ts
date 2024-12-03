import {MessageOutputModel} from "./MessagesOutputModel";


export type GetMessagesListOutputModel = {
    messages: MessageOutputModel[],
    size: number
}