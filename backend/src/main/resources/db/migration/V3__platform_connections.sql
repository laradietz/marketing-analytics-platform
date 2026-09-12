-- Supports connecting a real ad-platform account (Meta/Google/TikTok Ads) per
-- Platform, plus linking a Campaign to its external counterpart so metrics
-- can later be synced automatically instead of entered by hand.

CREATE TABLE platform_connections (
    id                      BIGSERIAL PRIMARY KEY,
    platform_id             BIGINT NOT NULL UNIQUE REFERENCES platforms(id),
    provider                VARCHAR(20) NOT NULL,
    status                  VARCHAR(20) NOT NULL,
    external_account_id     VARCHAR(120),
    access_token_encrypted  TEXT,
    refresh_token_encrypted TEXT,
    token_expires_at        TIMESTAMP,
    last_synced_at          TIMESTAMP,
    last_sync_message       VARCHAR(500),
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL
);

ALTER TABLE campaigns ADD COLUMN external_campaign_id VARCHAR(120);
