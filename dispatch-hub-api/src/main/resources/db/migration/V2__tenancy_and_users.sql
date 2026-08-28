CREATE TABLE tenant (
    id          UUID         PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenant_code UNIQUE (code)
);

CREATE TABLE app_user (
    id             UUID         PRIMARY KEY,
    username       VARCHAR(128) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

CREATE TABLE tenant_membership (
    id          UUID         PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenant (id),
    user_id     UUID         NOT NULL REFERENCES app_user (id),
    role        VARCHAR(16)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tenant_membership UNIQUE (tenant_id, user_id),
    CONSTRAINT ck_tenant_membership_role CHECK (role IN ('ADMIN', 'VIEWER'))
);

CREATE INDEX ix_tenant_membership_user ON tenant_membership (user_id);

-- Demo password for all seeded users: password
-- BCrypt cost 10
INSERT INTO tenant (id, code, name) VALUES
    ('11111111-1111-1111-1111-111111111111', 'acme', 'Acme Corp'),
    ('22222222-2222-2222-2222-222222222222', 'beta', 'Beta Industries');

INSERT INTO app_user (id, username, password_hash, enabled) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'admin.acme',
     '$2a$10$sbWH69S6poFwg7c2rrat4ePg1cOztpWZ2sWtAiANwKDhi5X/KJhnK', TRUE),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'viewer.acme',
     '$2a$10$sbWH69S6poFwg7c2rrat4ePg1cOztpWZ2sWtAiANwKDhi5X/KJhnK', TRUE),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'admin.beta',
     '$2a$10$sbWH69S6poFwg7c2rrat4ePg1cOztpWZ2sWtAiANwKDhi5X/KJhnK', TRUE),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'viewer.beta',
     '$2a$10$sbWH69S6poFwg7c2rrat4ePg1cOztpWZ2sWtAiANwKDhi5X/KJhnK', TRUE);

INSERT INTO tenant_membership (id, tenant_id, user_id, role) VALUES
    ('cccccccc-cccc-cccc-cccc-ccccccccccc1',
     '11111111-1111-1111-1111-111111111111',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'ADMIN'),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc2',
     '11111111-1111-1111-1111-111111111111',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'VIEWER'),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc3',
     '22222222-2222-2222-2222-222222222222',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'ADMIN'),
    ('cccccccc-cccc-cccc-cccc-ccccccccccc4',
     '22222222-2222-2222-2222-222222222222',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'VIEWER');
