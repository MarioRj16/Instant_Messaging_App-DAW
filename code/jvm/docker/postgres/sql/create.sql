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

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL UNIQUE CHECK (length(username) > 4),
    email VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,

    CONSTRAINT email_is_valid CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE TABLE auth_token (
    token UUID PRIMARY KEY,
    user_id INTEGER REFERENCES users (user_id),
    created_at BIGINT NOT NULL,
    last_used_at BIGINT NOT NULL
);

CREATE TABLE registration_invitation(
    invitation_token UUID PRIMARY KEY,
    inviter_id INTEGER REFERENCES users (user_id),
    created_at BIGINT NOT NULL,
    status invite_status NOT NULL DEFAULT 'pending'
);

CREATE TABLE channel (
    channel_id SERIAL PRIMARY KEY,
    owner_id INTEGER REFERENCES users (user_id),
    channel_name VARCHAR NOT NULL CHECK (length(channel_name) > 0),
    created_at BIGINT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE message(
    message_id SERIAL PRIMARY KEY,
    channel_id INTEGER REFERENCES channel (channel_id),
    sender_id INTEGER REFERENCES users (user_id),
    created_at BIGINT NOT NULL,
    content TEXT NOT NULL CHECK (length(content) > 0)
);

CREATE TABLE membership(
    membership_id SERIAL PRIMARY KEY,
    member_id INTEGER REFERENCES users (user_id),
    channel_id INTEGER REFERENCES channel (channel_id),
    role membership_role NOT NULL DEFAULT 'member',
    joined_at BIGINT NOT NULL,

    CONSTRAINT unique_membership UNIQUE (member_id, channel_id)
);

CREATE TABLE channel_invitation(
    channel_invitation_id SERIAL PRIMARY KEY,
    inviter_id INTEGER REFERENCES users (user_id),
    invitee_id INTEGER REFERENCES users (user_id),
    channel_id INTEGER REFERENCES channel (channel_id),
    role invite_role NOT NULL DEFAULT 'member',
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    status invite_status NOT NULL DEFAULT 'pending',

    -- one invitation per channel per invitee.
    -- This is to prevent multiple invitations to the same person for the same channel.
    -- It is needed because the invitee is immediately added to the channel upon accepting the invitation.

    CONSTRAINT created_before_expiration CHECK (created_at < expires_at)
);

CREATE UNIQUE INDEX unique_pending_or_expired_invitation ON channel_invitation (inviter_id, invitee_id, channel_id)
    WHERE status = 'pending' and created_at < expires_at;