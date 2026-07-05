-- Local fix (Cách 1): sync users → app_user so history.created_by satisfies both FKs.
-- Run after sample_data.sql on your machine only:
--   docker exec -i aims-postgres psql -U itss_group6 -d aims_db < Programming/aims/scripts/fix_app_user_from_users.sql
--
-- Does not change application code. Safe to skip if app_user already has rows.

INSERT INTO app_user (user_id, email, hashed_password, user_name, avatar_url, created_by)
SELECT u.user_id, u.email, u.hashed_password, u.user_name, u.avatar_url, u.created_by
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM app_user a WHERE a.user_id = u.user_id);

SELECT setval(
    pg_get_serial_sequence('app_user', 'user_id'),
    COALESCE((SELECT MAX(user_id) FROM app_user), 1)
);
