INSERT INTO auth_roles(id, role_name)
VALUES
    (1, 'ADMIN'),
    (2, 'USER')
ON CONFLICT (role_name) DO NOTHING;
ALTER TABLE auth_users
    DROP CONSTRAINT auth_users_status_check;

ALTER TABLE auth_users
    ADD CONSTRAINT auth_users_status_check
        CHECK (
            status IN (
                       'PENDING',
                       'ACTIVE',
                       'INACTIVE',
                       'BANNED'
                )
            );