
export function getAuthToken (): string | undefined{
    const cookieString = document.cookie;
    const cookies = cookieString.split('; ').reduce((acc: Record<string, string>, current) => {
        const [name, value] = current.split('=');
        acc[name] = value;
        return acc;
    }, {});
    return cookies['authToken'] || undefined;
}

export function setAuthToken (token: string) {
    const isProduction = process.env.NODE_ENV === 'production';
    const cookieAttributes = `path=/; SameSite=Strict; ${isProduction ? 'secure;' : ''}`;
    document.cookie = `authToken=${token}; ${cookieAttributes}`;
}

export function removeAuthToken() {
    const isProduction = process.env.NODE_ENV === 'production';
    const cookieAttributes = `path=/; SameSite=Strict; ${isProduction ? 'secure;' : ''}`;
    document.cookie = `authToken=; ${cookieAttributes} expires=Thu, 01 Jan 1970 00:00:00 UTC;`;
}

