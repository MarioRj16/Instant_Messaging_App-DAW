
INSERT INTO registration_invitation (invitation_code, created_at)
VALUES
    ('1ABCD', 1729382400),
    ('EF21K', 1729382410);

INSERT INTO users (invitation_code, username, password)
VALUES
    ('1ABCD', 'rxfa123', '$2a$12$8Edde3c7GlrCTxHFVlXasOWDmtY1RQ.3Vwh7BEu9RWc7fWNSodPJ.'),
    ('EF21K', 'admin123', '$2a$12$qWmekXEazYubKbsAzCTRPO9RwixCo1RlwyCpjD/MMN7rnir9l25wq');
