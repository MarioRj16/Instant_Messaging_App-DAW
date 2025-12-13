#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -v rxfa_pwd="$RXFA_PASSWORD_HASH" -v admin_pwd="$ADMIN_PASSWORD_HASH" <<-EOSQL
    INSERT INTO registration_invitation (invitation_code, created_at)
    VALUES
        ('1ABCD', 1729382400),
        ('EF21K', 1729382410);

    INSERT INTO users (invitation_code, username, password)
    VALUES
        ('1ABCD', 'rxfa123', :'rxfa_pwd'),
        ('EF21K', 'admin123', :'admin_pwd');
EOSQL
