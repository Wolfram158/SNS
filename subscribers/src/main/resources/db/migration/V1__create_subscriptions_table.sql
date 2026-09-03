CREATE TABLE IF NOT EXISTS subscriptions (
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,

    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id != following_id)
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_follower
    ON subscriptions (follower_id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_following
    ON subscriptions (following_id);