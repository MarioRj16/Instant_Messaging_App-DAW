
export function replaceParams(uri: string, params: { [key: string]: number | string }) {
    const paramNames = getParamNames(uri);
    paramNames.forEach(paramName => {
        const paramValue = params[paramName];
        // {&page,itemPerPage} => &page=3&itemPerPage=10
        // or {&page} => &page=3
        if (paramName.startsWith('&')) {
            // parse the query params
            // paramName can be [&page,itemsPerPage] or [&page]
            const queryParamNames = paramName.substring(1).split(',');
            let query = '';
            queryParamNames.forEach(queryParamName => {
                const queryParamValue = params[queryParamName];
                if (queryParamValue) {
                    query += `&${queryParamName}=${queryParamValue}`;
                }
            });
            uri = uri.replace(`{${paramName}}`, query.substring(1));
        } else if (paramValue) {
            uri = uri.replace(`{${paramName}}`, paramValue.toString());
        }
    });
    // ensure after ? there is no &
    uri = uri.replace('?&', '?');
    return uri;
}

function getParamNames(uri: string) {
    const paramNames = [];
    const regex = /{([^}]+)}/g;
    let match: Array<string> | null;
    while ((match = regex.exec(uri))) {
        paramNames.push(match[1]);
    }
    return paramNames;
}