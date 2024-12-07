import {callApi, Method} from "./Utils/CallAPI";
import {CreateChannelInputModel} from "../models/input/CreateChannelInputModel";
import {CreateChannelOutputModel} from "../models/output/CreateChannelOutputModel";
import {SendMessageInputModel} from "../models/input/SendMessageInputModel";
import {replaceParams} from "./Utils/ParamsUri";
import {GetChannelsListOutputModel} from "../models/output/GetChannelsListOutputModel";
import {InviteMemberInputModel} from "../models/input/InviteMemberInputModel";
import {GetMessagesListOutputModel} from "../models/output/GetMessagesListOutputModel";
import {getAuthToken} from "./Utils/CookiesHandling";
import {GetChannelInvitationsListOutputModel} from "../models/output/GetChannelInvitationsListOutputModel";
import {MessageOutputModel} from "../models/output/MessagesOutputModel";
import {ChannelOutputModel} from "../models/output/ChannelOutputModel";


export async function createChannel(body: CreateChannelInputModel) {
    const uri='/channel';
    const cookies= getAuthToken()
    return await callApi<CreateChannelInputModel, CreateChannelOutputModel>(uri, Method.POST, body,cookies);
}

export async function getChannels() {
    const uri='/channel';
    const cookies= getAuthToken()
    return await callApi<null, GetChannelsListOutputModel>(uri, Method.GET,undefined,cookies);
}


export async function getChannel(id: number) {
    const uri=replaceParams('/channel/{id}/settings',{id:id});
    const cookies= getAuthToken()
    return await callApi<null, ChannelOutputModel>(uri, Method.GET,undefined,cookies);
}

export async function searchChannels(channelName: string,page:number,pageSize:number) {
    const uri='/channel/search?channelName='+channelName+'&page='+page+'&pageSize='+pageSize;
    const cookies= getAuthToken()
    return await callApi<null, GetChannelsListOutputModel>(uri, Method.GET,undefined,cookies);
}

export async function joinChannel(id: number) {
    const uri= replaceParams('/channel/{id}/join',{id:id});
    const cookies= getAuthToken()
    return await callApi(uri, Method.POST,undefined,cookies);
}

export async function getMessages(id: number) {
    const uri= replaceParams('/channel/{id}',{id:id});

    const cookies= getAuthToken()
    return await callApi<null,GetMessagesListOutputModel>(uri, Method.GET,undefined,cookies);
}

export function listenToMessages(): EventSource {
    const uri = '/api/channel/listen'; // Ensure this matches your backend URI

    const eventSource = new EventSource(uri);
    eventSource.onopen = () => {
        console.log('Listening to messages...');

    };

    return eventSource;
}


export async function sendMessage(id: number,body: SendMessageInputModel) {
    const uri= replaceParams('/channel/{id}',{id:id});
    const cookies= getAuthToken()
    return await callApi<SendMessageInputModel, null>(uri, Method.POST, body, cookies);
}

export async function getListInvitations(){
    const uri='/channel/invitations';
    const cookies= getAuthToken()
    return await callApi<null, GetChannelInvitationsListOutputModel>(uri, Method.GET,undefined,cookies);
}

export async function acceptInvitation(id: number) {
    const uri= replaceParams('/channel/invitations/{id}/accept',{id:id});
    const cookies= getAuthToken()
    return await callApi(uri, Method.POST,undefined,cookies);
}

export async function declineInvitation(id: number) {
    const uri= replaceParams('/channel/invitations/{id}/decline',{id:id});
    const cookies= getAuthToken()
    return await callApi(uri, Method.POST,undefined,cookies);
}


export async function leaveChannel(id: number) {
    const uri= replaceParams('/channel/{id}/memberships',{id:id});
    const cookies= getAuthToken()
    return await callApi(uri, Method.DELETE, undefined,cookies);
}

export async function inviteMember(id:number, body:InviteMemberInputModel){
    const uri= replaceParams('/channel/{id}/memberships',{id:id});
    const cookies= getAuthToken()
    return await callApi<InviteMemberInputModel, null>(uri, Method.POST,body,cookies);
}



