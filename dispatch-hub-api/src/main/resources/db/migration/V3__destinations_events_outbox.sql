CREATE TABLE destination (
    id            UUID         PRIMARY KEY,
    tenant_id     UUID         NOT NULL REFERENCES tenant (id),
    name          VARCHAR(128) NOT NULL,
    target_url    VARCHAR(2048) NOT NULL,
    secret        VARCHAR(512),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_destination_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX ix_destination_tenant ON destination (tenant_id);

CREATE TABLE inbound_event (
    id               UUID         PRIMARY KEY,
    tenant_id        UUID         NOT NULL REFERENCES tenant (id),
    idempotency_key  VARCHAR(128) NOT NULL,
    payload          JSONB        NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inbound_event_idempotency UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX ix_inbound_event_tenant_created ON inbound_event (tenant_id, created_at DESC);

CREATE TABLE delivery_job (
    id               UUID         PRIMARY KEY,
    tenant_id        UUID         NOT NULL REFERENCES tenant (id),
    event_id         UUID         NOT NULL REFERENCES inbound_event (id),
    destination_id   UUID         NOT NULL REFERENCES destination (id),
    status           VARCHAR(16)  NOT NULL,
    attempt_count    INT          NOT NULL DEFAULT 0,
    next_run_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_error       VARCHAR(2000),
    locked_at        TIMESTAMPTZ,
    locked_by        VARCHAR(128),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_delivery_job_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'DEAD'))
);

CREATE INDEX ix_delivery_job_claim ON delivery_job (status, next_run_at);
CREATE INDEX ix_delivery_job_event ON delivery_job (tenant_id, event_id);

CREATE TABLE delivery_attempt (
    id              UUID         PRIMARY KEY,
    tenant_id       UUID         NOT NULL REFERENCES tenant (id),
    job_id          UUID         NOT NULL REFERENCES delivery_job (id),
    attempt_number  INT          NOT NULL,
    http_status     INT,
    duration_ms     BIGINT       NOT NULL,
    error_message   VARCHAR(2000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_delivery_attempt_job ON delivery_attempt (job_id, created_at);

-- Seed egress allowlist hosts for demo tenants (used by SSRF checks in a later milestone)
CREATE TABLE tenant_egress_host (
    id           UUID         PRIMARY KEY,
    tenant_id    UUID         NOT NULL REFERENCES tenant (id),
    host_pattern VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenant_egress_host UNIQUE (tenant_id, host_pattern)
);

INSERT INTO tenant_egress_host (id, tenant_id, host_pattern) VALUES
    ('dddddddd-dddd-dddd-dddd-ddddddddddd1', '11111111-1111-1111-1111-111111111111', 'localhost'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd2', '11111111-1111-1111-1111-111111111111', '127.0.0.1'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd3', '22222222-2222-2222-2222-222222222222', 'localhost'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd4', '22222222-2222-2222-2222-222222222222', '127.0.0.1');
