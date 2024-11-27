import API, { ApiResponse } from './APIConnection';
import {ProblemModel} from "../../models/ProblemModel";

const apiConnection = API();

export enum Method {
    GET = 'GET',
    POST = 'POST',
    PUT = 'PUT',
    DELETE = 'DELETE',
}


export async function callApi<B, T>(uri: string, method: Method, body?: B,token?:String): Promise<ApiResponse<T | ProblemModel>> {
    let response: ApiResponse<T>;
    let completeUri = `/api${uri}`;
    try {
        const bodyFormat = body ? body : {};
        switch (method) {
            case Method.GET:
                // @ts-ignore
                response = await apiConnection.getApi(completeUri,token);
                return response;

            case Method.POST:
                // @ts-ignore
                response = await apiConnection.postApi(completeUri, bodyFormat,token);
                return response;

            case Method.PUT:
                // @ts-ignore
                response = await apiConnection.putApi(completeUri, bodyFormat,token);
                return response;

            case Method.DELETE:
                // @ts-ignore
                response = await apiConnection.deleteApi(completeUri,token);
                return response;
        }
    } catch (error) {
        return (await error) as ApiResponse<ProblemModel>;
    }
}