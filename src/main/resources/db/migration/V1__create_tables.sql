CREATE TABLE url (
  id BIGSERIAL PRIMARY KEY,
  alias VARCHAR(128) UNIQUE,
  original_url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at TIMESTAMPTZ,
  click_count BIGINT NOT NULL DEFAULT 0,
  owner VARCHAR(128)
);

CREATE INDEX idx_url_alias ON url(alias);