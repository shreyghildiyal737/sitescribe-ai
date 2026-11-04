-- Real auth: store BCrypt password hash on each user account.
-- Nullable so the column can be added without breaking existing test data;
-- the application layer enforces that new registrations always set a password.
ALTER TABLE app_users ADD COLUMN password_hash VARCHAR(80);
