-- Audience feedback captured per platform (manually or imported from a CSV
-- export), used to surface recurring themes and feed the content generator
-- with real audience language.

CREATE TABLE audience_comments (
    id              BIGSERIAL PRIMARY KEY,
    platform_id     BIGINT NOT NULL REFERENCES platforms(id),
    content_id      BIGINT REFERENCES contents(id) ON DELETE SET NULL,
    author_name     VARCHAR(150),
    text            TEXT NOT NULL,
    sentiment       VARCHAR(10) NOT NULL,
    source          VARCHAR(10) NOT NULL,
    posted_at       DATE,
    created_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_audience_comments_platform ON audience_comments(platform_id);
CREATE INDEX idx_audience_comments_sentiment ON audience_comments(sentiment);
