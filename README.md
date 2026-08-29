# Dispatch Hub

Multi-tenant notification / webhook dispatcher take-home (WIP).

## Stack

- **API:** Spring Boot 4.1.1 (`dispatch-hub-api`), Java 25 LTS
- **Web:** Angular 22 (`dispatch-hub-web`)
- **DB:** PostgreSQL 16 via Docker Compose

## Setup

1. Copy env file (never commit `.env`):

   ```bash
   cp .env.example .env
   ```

2. Start Postgres:

   ```bash
   docker compose up -d
   ```

3. API (from `dispatch-hub-api/`):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Web (from `dispatch-hub-web/`):

   ```bash
   npm install
   npm start
   ```

Actuator health: `http://localhost:8080/actuator/health`

OpenAPI: `http://localhost:8080/swagger-ui.html`

## Demo users

Password for all demo accounts: `password`

| Username | Tenant | Role |
|----------|--------|------|
| `admin.acme` | `acme` | ADMIN |
| `viewer.acme` | `acme` | VIEWER |
| `admin.beta` | `beta` | ADMIN |
| `viewer.beta` | `beta` | VIEWER |

Login:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin.acme\",\"password\":\"password\",\"tenantCode\":\"acme\"}"
```

Use the returned `accessToken` as `Authorization: Bearer …` on tenant APIs.

### Destinations and events

- `POST /api/v1/tenants/{tenantId}/destinations` (ADMIN) — create webhook destination
- `POST /api/v1/tenants/{tenantId}/events` (ADMIN) — submit event with `idempotencyKey` + JSON `payload`; enqueues delivery jobs
- `GET /api/v1/tenants/{tenantId}/events/{eventId}` — event + job statuses
- `GET /api/v1/tenants/{tenantId}/jobs/{jobId}/attempts` — delivery attempts

Dispatch runs on a scheduled DB outbox poller (`FOR UPDATE SKIP LOCKED`), no message broker.

## Security notes

Webhook destinations are checked in two steps (see [docs/decisions.md](docs/decisions.md) ADR-001):

1. **Global deny** — block private, loopback, link-local, and cloud metadata addresses (after DNS resolution).
2. **Per-tenant allowlist** — only hosts/CIDRs configured for that tenant may be used.

Local/demo profiles may allow `localhost` for WireMock and manual tests. Do not put secrets in git; use `.env` from `.env.example`.

## CI

Pull requests and pushes to `master` run GitHub Actions (`.github/workflows/ci.yml`):

- **API:** Temurin JDK 25, Postgres 16 service, `./mvnw verify`
- **Web:** Node 22, `npm ci` and `npm run build`

## Design decisions

Rationale for Java version, async delivery without a broker, caching, and SSRF layering: [docs/decisions.md](docs/decisions.md).
