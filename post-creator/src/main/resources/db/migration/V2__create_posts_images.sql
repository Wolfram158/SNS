CREATE TABLE IF NOT EXISTS post_images (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    s3_key TEXT NOT NULL,
    position INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_post_images_post_id ON post_images (post_id);