CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_nickname UNIQUE (nickname),
    CONSTRAINT chk_users_nickname_length CHECK (char_length(nickname) BETWEEN 3 AND 32)
);

CREATE INDEX idx_users_nickname ON users (nickname);