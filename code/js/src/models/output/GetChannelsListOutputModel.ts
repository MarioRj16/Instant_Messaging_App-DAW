import {ChannelOutputModel} from "./ChannelOutputModel";

export type GetChannelsListOutputModel = {
    channels: ChannelOutputModel[],
    pageSize: number,
    page: number,
    totalPages: number,
    totalSize: number,
    hasPrevious: boolean,
    hasNext: boolean,
    previousPage: number | null,
    nextPage: number | null,
    size: number
}

