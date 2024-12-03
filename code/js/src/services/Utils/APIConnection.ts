export type ApiResponse<T> = {
    contentType: string | null;
    json: T;
};

type Options = {
    method: string;
    body?: BodyInit;
    token?: string; // Optional Bearer token for authorization
};

export default function () {
    return {
        getApi: getApi,
        postApi: postApi,
        deleteApi: deleteApi,
        putApi: putApi,
    };

    async function fetchApi<T>(path: string, options: Options): Promise<ApiResponse<T>> {
        // Prepare headers with or without token
        const headers: HeadersInit = {
            'Content-Type': 'application/json', // Default content-type
            ...(options.token && { Authorization: `Bearer ${options.token}` }), // Add Authorization if token exists
        };

        // Merge headers into options
        const fetchOptions: RequestInit = {
            method: options.method,
            headers,
            body: options.body,
        };

        const response = await fetch(path, fetchOptions);
        const contentType = response.headers.get('content-type');
        const json = await response.json();
        return {
            contentType: contentType,
            json: json,
        };
    }

    function getApi<T>(path: string, token?: string): Promise<ApiResponse<T>> {
        const options = {
            method: 'GET',
            token,
        };
        return fetchApi<T>(path, options);
    }

    function postApi<T, R>(path: string, body: T, token?: string): Promise<ApiResponse<R>> {
        const options = {
            method: 'POST',
            body: JSON.stringify(body),
            token,
        };
        return fetchApi<R>(path, options);
    }

    function deleteApi<T>(path: string, token?: string): Promise<ApiResponse<T>> {
        const options = {
            method: 'DELETE',
            token,
        };
        return fetchApi<T>(path, options);
    }

    function putApi<T, R>(path: string, body: T, token?: string): Promise<ApiResponse<R>> {
        const options = {
            method: 'PUT',
            body: JSON.stringify(body),
            token,
        };
        return fetchApi<R>(path, options);
    }
}
