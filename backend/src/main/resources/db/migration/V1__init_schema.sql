-- Core schema for the Marketing Analytics Platform.

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(180) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    job_title       VARCHAR(120),
    company_name    VARCHAR(150),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE platforms (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(60) NOT NULL,
    slug        VARCHAR(60) NOT NULL,
    color_hex   VARCHAR(7),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_platforms_name UNIQUE (name),
    CONSTRAINT uk_platforms_slug UNIQUE (slug)
);

CREATE TABLE campaigns (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    description         TEXT,
    platform_id         BIGINT NOT NULL REFERENCES platforms(id),
    objective           VARCHAR(20) NOT NULL,
    budget              NUMERIC(14,2) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    status              VARCHAR(20) NOT NULL,
    target_audience     TEXT,
    owner_id            BIGINT REFERENCES users(id),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_platform ON campaigns(platform_id);
CREATE INDEX idx_campaigns_owner ON campaigns(owner_id);

CREATE TABLE campaign_metrics (
    id              BIGSERIAL PRIMARY KEY,
    campaign_id     BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    recorded_date   DATE NOT NULL,
    impressions     BIGINT NOT NULL DEFAULT 0,
    reach           BIGINT NOT NULL DEFAULT 0,
    clicks          BIGINT NOT NULL DEFAULT 0,
    conversions     BIGINT NOT NULL DEFAULT 0,
    spend           NUMERIC(14,2) NOT NULL DEFAULT 0,
    revenue         NUMERIC(14,2) NOT NULL DEFAULT 0,
    likes           BIGINT NOT NULL DEFAULT 0,
    comments        BIGINT NOT NULL DEFAULT 0,
    shares          BIGINT NOT NULL DEFAULT 0,
    saves           BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL,
    CONSTRAINT uk_campaign_metric_date UNIQUE (campaign_id, recorded_date)
);
CREATE INDEX idx_campaign_metrics_date ON campaign_metrics(recorded_date);

CREATE TABLE daily_metrics (
    id              BIGSERIAL PRIMARY KEY,
    metric_date     DATE NOT NULL,
    platform_id     BIGINT NOT NULL REFERENCES platforms(id),
    impressions     BIGINT NOT NULL DEFAULT 0,
    clicks          BIGINT NOT NULL DEFAULT 0,
    conversions     BIGINT NOT NULL DEFAULT 0,
    spend           NUMERIC(14,2) NOT NULL DEFAULT 0,
    revenue         NUMERIC(14,2) NOT NULL DEFAULT 0,
    CONSTRAINT uk_daily_metric_date_platform UNIQUE (metric_date, platform_id)
);

CREATE TABLE ads (
    id          BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    format      VARCHAR(20) NOT NULL,
    headline    VARCHAR(200),
    body        TEXT,
    cta_label   VARCHAR(60),
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP NOT NULL
);
CREATE INDEX idx_ads_campaign ON ads(campaign_id);

CREATE TABLE budgets (
    id              BIGSERIAL PRIMARY KEY,
    campaign_id     BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    planned_amount  NUMERIC(14,2) NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_budgets_campaign ON budgets(campaign_id);

CREATE TABLE leads (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    email           VARCHAR(180) NOT NULL,
    phone           VARCHAR(30),
    company         VARCHAR(150),
    source          VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    score           INT NOT NULL DEFAULT 0,
    contact_count   INT NOT NULL DEFAULT 0,
    campaign_id     BIGINT REFERENCES campaigns(id),
    assigned_to     BIGINT REFERENCES users(id),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_campaign ON leads(campaign_id);

CREATE TABLE contents (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    copy_text       TEXT,
    platform_id     BIGINT NOT NULL REFERENCES platforms(id),
    objective       VARCHAR(20),
    status          VARCHAR(20) NOT NULL,
    scheduled_date  DATE,
    published_date  DATE,
    hashtags        TEXT,
    cta_text        VARCHAR(150),
    campaign_id     BIGINT REFERENCES campaigns(id),
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_contents_status ON contents(status);
CREATE INDEX idx_contents_scheduled_date ON contents(scheduled_date);

CREATE TABLE content_ideas (
    id                  BIGSERIAL PRIMARY KEY,
    product_or_service  VARCHAR(200) NOT NULL,
    target_audience     VARCHAR(300),
    platform_id         BIGINT REFERENCES platforms(id),
    objective           VARCHAR(20),
    tone                VARCHAR(20) NOT NULL,
    generated_title     VARCHAR(200),
    generated_copy      TEXT,
    generated_cta       VARCHAR(150),
    generated_hashtags  TEXT,
    status              VARCHAR(25) NOT NULL,
    created_by          BIGINT REFERENCES users(id),
    created_at          TIMESTAMP NOT NULL
);

CREATE TABLE recommendations (
    id                      BIGSERIAL PRIMARY KEY,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT NOT NULL,
    type                    VARCHAR(25) NOT NULL,
    priority                VARCHAR(10) NOT NULL,
    related_metric_name     VARCHAR(40),
    related_metric_value    NUMERIC(14,4),
    campaign_id             BIGINT REFERENCES campaigns(id) ON DELETE CASCADE,
    platform_id             BIGINT REFERENCES platforms(id),
    status                  VARCHAR(15) NOT NULL,
    created_at              TIMESTAMP NOT NULL
);
CREATE INDEX idx_recommendations_status ON recommendations(status);

CREATE TABLE goals (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    metric_type     VARCHAR(20) NOT NULL,
    target_value    NUMERIC(14,2) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    campaign_id     BIGINT REFERENCES campaigns(id) ON DELETE CASCADE,
    created_by      BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL
);

CREATE TABLE reports (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    campaign_ids    TEXT,
    platform_ids    TEXT,
    summary_json    TEXT NOT NULL,
    generated_by    BIGINT REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL
);

CREATE TABLE activity_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id),
    action          VARCHAR(60) NOT NULL,
    entity_type     VARCHAR(40) NOT NULL,
    entity_id       BIGINT,
    description     VARCHAR(300) NOT NULL,
    created_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);
