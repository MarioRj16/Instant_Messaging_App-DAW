INSERT INTO users (username, email, password)
VALUES
    ('rxfa123', 'rafa3rnrn@gmail.com', '$2a$12$8Edde3c7GlrCTxHFVlXasOWDmtY1RQ.3Vwh7BEu9RWc7fWNSodPJ.'),
    ('admin123', 'mariorijocarvalho@gmail.com', '$2a$12$qWmekXEazYubKbsAzCTRPO9RwixCo1RlwyCpjD/MMN7rnir9l25wq');

INSERT INTO registration_invitation (invitation_token, inviter_id, created_at, status)
VALUES
    ('f7b3b3b4-1b3b-4b3b-8b3b-3b3b3b3b3b3b', 1, '2021-01-01 00:00:00', 'pending');
