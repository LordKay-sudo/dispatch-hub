# Dispatch Hub

Multi-tenant notification / webhook dispatcher take-home.

## Stack

- **API:** Spring Boot 4.1.1 (`dispatch-hub-api`), Java 25 LTS
- **Persistence:** JPA for domain CRUD; Spring Data JDBC for outbox claim / SKIP LOCKED (see [ADR-008](docs/decisions.md))
- **Web:** Angular 22 + Material (`dispatch-hub-web`)
- **DB:** PostgreSQL 16 via Docker Compose
- **AI:** Spring AI 2 (OpenAI optional; mock default)

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

| Link | URL |
|------|-----|
| Health | http://localhost:8080/actuator/health |
| Metrics | http://localhost:8080/actuator/metrics |
| Prometheus | http://localhost:8080/actuator/prometheus |
| OpenAPI | http://localhost:8080/swagger-ui.html |
| Web UI | http://localhost:4200 |

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

### Main APIs

- `POST /api/v1/tenants/{tenantId}/destinations` (ADMIN)
- `POST /api/v1/tenants/{tenantId}/events` (ADMIN) — idempotency key + payload
- `GET /api/v1/tenants/{tenantId}/events/{eventId}`
- `GET /api/v1/tenants/{tenantId}/jobs/{jobId}/attempts`
- `POST /api/v1/tenants/{tenantId}/jobs/{jobId}/retry` (ADMIN)
- `POST /api/v1/tenants/{tenantId}/jobs/{jobId}/ai-summary`
- `GET /api/v1/tenants/{tenantId}/ops-summary` — job counts by status

Set `AI_PROVIDER=mock|openai` (default `mock`). With `openai`, set `OPENAI_API_KEY`. No Embabel; no shared AI memory across tenants.

Dispatch uses a DB outbox poller (`FOR UPDATE SKIP LOCKED`), exponential backoff, and terminal **DEAD**. No message broker in the demo.

## Tests

```bash
# API (needs Postgres on localhost:5432 — use docker compose)
cd dispatch-hub-api && ./mvnw verify

# Web unit smoke
cd dispatch-hub-web && npm test -- --run
```

CI runs the same API verify + web build on every PR. Playwright end-to-end against a full stack is documented as a follow-up (happy-path: login → event → attempt); not required in default CI.

## Security notes

See [ADR-001](docs/decisions.md): global SSRF deny, then per-tenant egress allowlist. Demo allowlists include `localhost` / `127.0.0.1`. Never commit secrets.

## Observability

- `X-Request-Id` on responses (generated if missing)
- Micrometer counters: `dispatchhub.delivery.success|retry|dead|rate_limited`
- Actuator `health`, `metrics`, `prometheus` (scrape on VPS; Grafana optional)

## Production readiness (short)

| Topic | Demo | Next step on VPS |
|-------|------|------------------|
| Async | DB outbox | RabbitMQ/SQS workers |
| Rate limits | Caffeine | Redis |
| Scale | One API+worker JVM | Stateless API replicas + shared claim SQL |
| Secrets | `.env` | Docker/OS secrets + TLS via Nginx |
| Backup | Compose volume | `pg_dump` / snapshots |
| AI | Mock or OpenAI key | Budget alerts + stricter redaction |

## Architecture & decisions

- [docs/architecture.md](docs/architecture.md)
- [docs/decisions.md](docs/decisions.md)

## CI

PRs/pushes to `master`: JDK 25 + Postgres API tests; Node 22 web build.

## Tools used

Cursor (agent-assisted implementation and reviews). All submitted code was reviewed and is expected to be explainable/modifiable in a live demo.

## Submission

Git tag for the review build: **`submission`** (points at the final master commit for this assessment).

Checklist for reviewers:

- [x] Working API + Angular UI
- [x] Flyway migrations + demo seed users
- [x] JWT auth with ADMIN / VIEWER
- [x] Async outbox dispatch (no message broker)
- [x] SSRF + tenant egress allowlist
- [x] Retries / DEAD + manual retry
- [x] AI failure summarizer (mock by default)
- [x] Automated API tests in CI + web build
- [x] OpenAPI / Swagger UI
- [x] Docker Compose for Postgres
- [x] `.env.example` only (no committed secrets)
- [x] Architecture + decision docs

To run a local demo after clone: follow **Setup**, log in as `admin.acme` / `password` / tenant `acme`, create a destination pointing at a reachable allowlisted host (`localhost` / `127.0.0.1` in demo), submit an event, watch job attempts, optionally call AI summary.
