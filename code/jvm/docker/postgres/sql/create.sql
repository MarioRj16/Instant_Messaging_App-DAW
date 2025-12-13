\set cascade 'ON DELETE CASCADE ON UPDATE CASCADE'
\set ref_users 'REFERENCES users (user_id)'
\set ref_channel 'REFERENCES channel (channel_id)'
\set member 'member'
\set viewer 'viewer'

CREATE TYPE invite_status AS ENUM ('pending', 'accepted', 'rejected');
CREATE TYPE membership_role AS ENUM ('owner', :'member', :'viewer');
CREATE TYPE invite_role AS ENUM (:'member', :'viewer');

CREATE TABLE registration_invitation(
    invitation_code VARCHAR(50) PRIMARY KEY,
    created_at BIGINT NOT NULL
);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    invitation_code VARCHAR(50) REFERENCES registration_invitation (invitation_code) :cascade,
    username VARCHAR(64) NOT NULL UNIQUE CHECK (length(username) > 4),
    password VARCHAR(255) NOT NULL
);

CREATE TABLE auth_token (
    token UUID PRIMARY KEY,
    user_id INTEGER :ref_users :cascade,
    created_at BIGINT NOT NULL,
    last_used_at BIGINT NOT NULL
);

CREATE TABLE channel (
    channel_id SERIAL PRIMARY KEY,
    owner_id INTEGER :ref_users :cascade,
    channel_name VARCHAR(64) NOT NULL UNIQUE,
    created_at BIGINT NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE message(
    message_id SERIAL PRIMARY KEY,
    channel_id INTEGER :ref_channel :cascade,
    sender_id INTEGER :ref_users :cascade,
    created_at BIGINT NOT NULL,
    content TEXT NOT NULL CHECK (length(content) > 0)
);

CREATE TABLE membership(
    membership_id SERIAL PRIMARY KEY,
    member_id INTEGER :ref_users :cascade,
    channel_id INTEGER :ref_channel :cascade,
    role membership_role NOT NULL DEFAULT :'member',
    joined_at BIGINT NOT NULL,

    CONSTRAINT unique_membership UNIQUE (member_id, channel_id)
);

CREATE TABLE channel_invitation(
    channel_invitation_id SERIAL PRIMARY KEY,
    inviter_id INTEGER :ref_users :cascade,
    invitee_id INTEGER :ref_users :cascade,
    channel_id INTEGER :ref_channel :cascade,
    role invite_role NOT NULL DEFAULT :'member',
    created_at BIGINT NOT NULL,

    constraint unique_channel_invitation UNIQUE (inviter_id, invitee_id, channel_id)
);

CREATE UNIQUE INDEX unique_pending_or_expired_invitation ON channel_invitation (inviter_id, invitee_id, channel_id);
