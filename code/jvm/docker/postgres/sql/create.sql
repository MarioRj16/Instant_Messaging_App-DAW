DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invite_status') THEN
            CREATE TYPE invite_status AS ENUM ('pending', 'accepted', 'rejected');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'membership_role') THEN
            CREATE TYPE membership_role AS ENUM ('owner', 'member', 'viewer');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invite_role') THEN
            CREATE TYPE invite_role AS ENUM ('member', 'viewer');
        END IF;
    END $$;

CREATE TABLE registration_invitation(
    invitation_code VARCHAR PRIMARY KEY,
    created_at BIGINT NOT NULL
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    invitation_code VARCHAR REFERENCES registration_invitation (invitation_code) ON DELETE CASCADE ON UPDATE CASCADE,
    username VARCHAR NOT NULL UNIQUE CHECK (length(username) > 4),
    password VARCHAR NOT NULL
);

CREATE TABLE auth_token (
    token UUID PRIMARY KEY,
    user_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at BIGINT NOT NULL,
    last_used_at BIGINT NOT NULL
);

CREATE TABLE channel (
    channel_id SERIAL PRIMARY KEY,
    owner_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    channel_name VARCHAR NOT NULL UNIQUE,
    created_at BIGINT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE message(
    message_id SERIAL PRIMARY KEY,
    channel_id INTEGER REFERENCES channel (channel_id) ON DELETE CASCADE ON UPDATE CASCADE,
    sender_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at BIGINT NOT NULL,
    content TEXT NOT NULL CHECK (length(content) > 0)
);

CREATE TABLE membership(
    membership_id SERIAL PRIMARY KEY,
    member_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    channel_id INTEGER REFERENCES channel (channel_id) ON DELETE CASCADE ON UPDATE CASCADE,
    role membership_role NOT NULL DEFAULT 'member',
    joined_at BIGINT NOT NULL,

    CONSTRAINT unique_membership UNIQUE (member_id, channel_id)
);

CREATE TABLE channel_invitation(
    channel_invitation_id SERIAL PRIMARY KEY,
    inviter_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    invitee_id INTEGER REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    channel_id INTEGER REFERENCES channel (channel_id) ON DELETE CASCADE ON UPDATE CASCADE,
    role invite_role NOT NULL DEFAULT 'member',
    created_at BIGINT NOT NULL,

    constraint unique_channel_invitation UNIQUE (inviter_id, invitee_id, channel_id)
);

CREATE UNIQUE INDEX unique_pending_or_expired_invitation ON channel_invitation (inviter_id, invitee_id, channel_id);