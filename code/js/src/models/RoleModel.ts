export type RoleModel = 'owner' | 'member' | 'viewer';

export const RoleModel = {
    owner: 'owner',
    member: 'member',
    viewer: 'viewer',
} as const;
