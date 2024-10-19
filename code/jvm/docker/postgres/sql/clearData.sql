-- Disable foreign key checks to avoid conflicts while truncating data
ALTER TABLE channel_invitation DISABLE TRIGGER ALL;
ALTER TABLE membership DISABLE TRIGGER ALL;
ALTER TABLE message DISABLE TRIGGER ALL;
ALTER TABLE channel DISABLE TRIGGER ALL;
ALTER TABLE registration_invitation DISABLE TRIGGER ALL;
ALTER TABLE auth_token DISABLE TRIGGER ALL;
ALTER TABLE account DISABLE TRIGGER ALL;


TRUNCATE TABLE
    channel_invitation,
    membership,
    message,
    channel,
    registration_invitation,
    auth_token,
    account
    RESTART IDENTITY CASCADE;

-- Re-enable foreign key checks
ALTER TABLE channel_invitation ENABLE TRIGGER ALL;
ALTER TABLE membership ENABLE TRIGGER ALL;
ALTER TABLE message ENABLE TRIGGER ALL;
ALTER TABLE channel ENABLE TRIGGER ALL;
ALTER TABLE registration_invitation ENABLE TRIGGER ALL;
ALTER TABLE auth_token ENABLE TRIGGER ALL;
ALTER TABLE account ENABLE TRIGGER ALL;
