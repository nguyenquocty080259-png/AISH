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

select *
from auth_email_verifications;

select *
from auth_email_verifications
order by id desc;