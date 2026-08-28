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

Actuator health: `http://localhost:8080/actuator/health` (once the API is up and security is configured for local use).

## Security notes

Webhook destinations are checked in two steps (see [docs/decisions.md](docs/decisions.md) ADR-001):

1. **Global deny** — block private, loopback, link-local, and cloud metadata addresses (after DNS resolution).
2. **Per-tenant allowlist** — only hosts/CIDRs configured for that tenant may be used.

Local/demo profiles may allow `localhost` for WireMock and manual tests. Do not put secrets in git; use `.env` from `.env.example`.

## Design decisions

Rationale for Java version, async delivery without a broker, caching, and SSRF layering: [docs/decisions.md](docs/decisions.md).
