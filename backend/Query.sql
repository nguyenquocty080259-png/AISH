SELECT * FROM auth_roles;

SELECT
    id,
    identifier,
    password_hash
FROM auth_accounts;

SELECT
    u.id,
    u.full_name,
    r.role_name
FROM auth_users u
         JOIN auth_user_roles ur
              ON ur.user_id = u.id
         JOIN auth_roles r
              ON r.id = ur.role_id;