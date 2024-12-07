import {callApi, Method} from "./Utils/CallAPI";
import {LoginInputModel} from "../models/input/LoginInputModel";
import {RegisterInputModel} from "../models/input/RegisterInputModel";
import {RegisterOutputModel} from "../models/output/RegisterOutputModel";
import {LoginOutputModel} from "../models/output/LoginOutputModel";
import {InviteOutputModel} from "../models/output/InviteOutputModel";
import {UserOutputModel} from "../models/output/UserOutputModel";
import {getAuthToken} from "./Utils/CookiesHandling";


export async function register(body: RegisterInputModel) {
    const uri='/users';
    return await callApi<RegisterInputModel, RegisterOutputModel>(uri, Method.POST, body);
}

export async function login(body: LoginInputModel) {
    const uri='/login';
    return await callApi<LoginInputModel, LoginOutputModel>(uri, Method.POST, body);
}

export async function logout() {
    const uri='/logout';
    const cookies= getAuthToken()
    return await callApi(uri, Method.POST,undefined,cookies);
}

export async function invite() {
    const uri='/invite';
    const cookies= getAuthToken()
    return await callApi<null,InviteOutputModel>(uri, Method.POST, undefined,cookies);
}

export async function me(){
    const uri='/me';
    const cookies= getAuthToken()
    return await callApi<null,UserOutputModel>(uri, Method.GET,undefined,cookies);
}