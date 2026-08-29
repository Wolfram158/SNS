CREATE TABLE IF NOT EXISTS feed_items (
    id BIGSERIAL PRIMARY KEY,
    post_id UUID NOT NULL,
    author_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    image_keys JSONB NOT NULL DEFAULT '[]'::JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_feed_items_post_id UNIQUE (post_id)
);

CREATE INDEX IF NOT EXISTS idx_feed_author_time
    ON feed_items (author_id, created_at DESC);