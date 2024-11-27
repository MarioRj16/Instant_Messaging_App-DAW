
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
    document.cookie = `authToken=${token}; path=/; secure; HttpOnly; SameSite=Strict`;
}

export function removeAuthToken() {
    document.cookie = `authToken=; path=/; secure; HttpOnly; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 UTC;`;
}
