ALTER TABLE posts ADD COLUMN author_id BIGINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts (author_id);
CREATE INDEX IF NOT EXISTS idx_posts_author_created ON posts (author_id, created_at)