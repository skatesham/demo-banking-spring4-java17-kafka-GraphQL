CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE account RENAME COLUMN holder_id TO user_id;
ALTER TABLE account ADD CONSTRAINT account_user_id_unique UNIQUE (user_id);
ALTER TABLE account ADD CONSTRAINT account_user_id_fk FOREIGN KEY (user_id) REFERENCES app_user (id);
