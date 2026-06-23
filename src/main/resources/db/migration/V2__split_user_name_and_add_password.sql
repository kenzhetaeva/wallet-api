ALTER TABLE users
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN password VARCHAR(60);

UPDATE users
SET first_name = COALESCE(NULLIF(split_part(name, ' ', 1), ''), name),
    last_name = COALESCE(NULLIF(substring(name FROM position(' ' IN name) + 1), ''), name),
    password = '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiVI6A6q1z3ZCn85/1ejiU3EDmvPuAa'
WHERE first_name IS NULL
   OR last_name IS NULL
   OR password IS NULL;

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ALTER COLUMN password SET NOT NULL,
    DROP COLUMN name;
