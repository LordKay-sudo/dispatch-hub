# Dispatch Hub

Multi-tenant notification / webhook dispatcher take-home.

## Stack

- **API:** Spring Boot 4.1.1 (`dispatch-hub-api`), Java 25 LTS
- **Persistence:** JPA for domain CRUD; Spring Data JDBC for outbox claim / SKIP LOCKED (see [ADR-008](docs/decisions.md))
- **Web:** Angular 22 + Material (`dispatch-hub-web`)
- **Runtime:** Docker Compose — Postgres 16, API, nginx UI, in-compose webhook echo
- **AI:** Spring AI 2 (OpenAI optional; mock default)

## Quick start (full stack)

Requires Docker Desktop (or compatible Compose).

1. Copy env file (never commit `.env`):

   ```bash
   cp .env.example .env
   ```

2. Build and run everything:

   ```bash
   docker compose up --build
   ```

3. Open the UI at **http://localhost:8080** (nginx → Angular; `/api` proxied to the API).

| Link | URL |
|------|-----|
| Web UI | http://localhost:8080 |
| Health (via nginx) | http://localhost:8080/actuator/health |
| OpenAPI (via nginx) | http://localhost:8080/swagger-ui.html |
| API direct | http://localhost:8081 |
| Prometheus (direct) | http://localhost:8081/actuator/prometheus |

Compose also starts **`webhook-echo`** for end-to-end delivery demos. Create a destination with target URL:

`http://webhook-echo:5678/`

(`SSRF_ALLOW_PRIVATE=true` in Compose so Docker DNS / RFC1918 is allowed for that demo target. Keep it `false` outside Docker.)

Stop with `docker compose down`. Data persists in the `postgres_data` volume.

## Hybrid local setup (optional)

Use this when iterating without rebuilding images:

1. `cp .env.example .env` then set `SSRF_ALLOW_PRIVATE=false` for host-only runs.
2. Start only Postgres: `docker compose up -d postgres`
3. API: `cd dispatch-hub-api && ./mvnw spring-boot:run`
4. Web: `cd dispatch-hub-web && npm install && npm start` → http://localhost:4200

Host destinations can use `http://127.0.0.1:<port>/` (seeded allowlist + `SSRF_ALLOW_LOOPBACK`).

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

Use the returned `accessToken` as `Authorization: Bearer …` on tenant APIs. Against the Compose stack, prefer the nginx port (`8080`); `8081` hits the API container directly.

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
# API (needs Postgres on localhost:5432 — use `docker compose up -d postgres`)
cd dispatch-hub-api && ./mvnw verify

# Web unit smoke
cd dispatch-hub-web && npm test -- --watch=false

# Playwright UI smoke (Compose stack must be up on http://localhost:8080)
cd dispatch-hub-web && npm run e2e
```

CI runs API verify + web build on every PR. Playwright is local/manual against Compose (login → destination → event SUCCESS; viewer cannot create destinations).

## Security notes

See [ADR-001](docs/decisions.md): global SSRF deny, then per-tenant egress allowlist. Demo allowlists include `localhost` / `127.0.0.1` / `webhook-echo`. Never commit secrets.

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
| SSRF private IPs | Allowed in Compose only | Keep `SSRF_ALLOW_PRIVATE=false` |

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
- [x] Docker Compose full stack (Postgres + API + nginx web + webhook echo)
- [x] `.env.example` only (no committed secrets)
- [x] Architecture + decision docs

To run a local demo after clone: `docker compose up --build`, open http://localhost:8080, log in as `admin.acme` / `password` / tenant `acme`, create a destination pointing at `http://webhook-echo:5678/`, submit an event, watch job attempts, optionally call AI summary.
