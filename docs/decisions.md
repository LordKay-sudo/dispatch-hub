# Design decisions

Short records of choices that affect security, ops, or review. Implementation may land after the scaffold; the intended behaviour is fixed here.

## ADR-001: Outbound URL checks use global deny then tenant allowlist

**Status:** Accepted (implemented)

**Context:** Destinations accept webhook URLs. Arbitrary URLs invite SSRF (cloud metadata, link-local, internal RFC1918). Tenants must also not call each other’s hosts or arbitrary public endpoints outside their egress policy.

**Decision:** Validate in two layers, in order, on create/update **and** again at dispatch time:

1. **Global deny** — Reject loopback, private, link-local, and cloud metadata ranges after DNS resolution (check resolved IPs, not only the hostname string). Non-HTTP(S) schemes are rejected. A local/demo profile may allow `localhost` / WireMock for tests only.
2. **Per-tenant allowlist** — The destination host must match that tenant’s configured allowed domains or CIDRs. Tenant A’s allowlist does not authorize Tenant B.

**Why both:** An allowlist alone is unsafe if a tenant (or bug) adds `169.254.169.254` or another blocked target. Global deny closes that class of SSRF. The tenant allowlist limits blast radius and enforces tenancy for egress.

**Consequences:** Destination CRUD needs allowlist admin (seeded per demo tenant). Dispatch fails closed if either check fails. Documented in the README under security.

## ADR-002: Java 25 LTS instead of 26

**Status:** Accepted

**Context:** Spring Boot 4.1 supports Java 21–26. Non-LTS 26 is available; 25 is LTS.

**Decision:** Target **Java 25 LTS**.

**Why:** Longer support and a clearer VPS/production story. This codebase does not need Java 26-only APIs.

## ADR-003: Database outbox instead of a message broker (demo)

**Status:** Accepted

**Context:** Dispatch must be asynchronous with retries, backoff, and a dead-letter state. Brokers are optional in the brief (“in-process equivalent” is allowed).

**Decision:** Persist work in Postgres and claim with `FOR UPDATE SKIP LOCKED`. No Kafka/RabbitMQ in default Compose.

**Why:** Enough for the demo and a single VPS; attempt history and idempotency stay in the DB. README notes RabbitMQ/SQS when polling no longer keeps up.

## ADR-004: Caffeine for cache and soft rate limits

**Status:** Accepted

**Context:** Hot destination reads and per-destination throttling need a cache. Redis is another option.

**Decision:** Spring Cache + **Caffeine** on the API JVM. Authoritative delivery state remains in Postgres.

**Why:** No extra process on a small VPS. Multi-node strict rate limits would move to Redis later; document that upgrade path.

## ADR-005: CI on every pull request

**Status:** Accepted

**Context:** Milestone work should be reviewable with automated checks before merge.

**Decision:** GitHub Actions workflow runs API tests (JDK 25 + Postgres service) and a web production build (Node 22) on pull requests and pushes to `master`.

**Why:** Catches breakages early without a local-only ritual. Heavier suites (Testcontainers matrix, Playwright) can join the same workflow later.

## ADR-006: Spring AI without Embabel or shared memory

**Status:** Accepted

**Context:** The brief needs a small failure summarizer, not a multi-step agent.

**Decision:** Use **Spring AI** behind a provider interface. No Embabel. No cross-tenant chat memory — each summarize call is stateless with sanitized, tenant-owned attempt metadata only.

**Why:** Matches scoring expectations with less complexity. Tenant isolation is enforced by input scope, not by a shared memory store.

## ADR-007: Retry backoff then DEAD (demo DLQ)

**Status:** Accepted

**Context:** Failed webhooks need retries and a terminal state.

**Decision:** Exponential backoff via `next_run_at` on the outbox row; after `app.dispatch.max-attempts`, mark **DEAD**. Manual `POST .../jobs/{id}/retry` requeues FAILED/DEAD for ADMIN.

**Why:** Keeps DLQ semantics in Postgres without a broker.

## ADR-008: JPA for domain CRUD, Spring Data JDBC for outbox claims

**Status:** Accepted

**Context:** Most entities are ordinary CRUD. Outbox claiming needs `FOR UPDATE SKIP LOCKED` and `UPDATE … RETURNING`, which do not fit JPA cleanly. Plain `JdbcTemplate` works but bypasses the Spring Data repository model we want for JDBC paths.

**Decision:**
- **JPA** — tenants, users, memberships, destinations, events, delivery job/attempt reads and normal writes.
- **Spring Data JDBC** — outbox claim and status transitions in `com.lordkay.dispatchhub.dispatch` (`OutboxClaimRepository` + custom fragment). Use `NamedParameterJdbcTemplate` inside that fragment for the SKIP LOCKED SQL; do not introduce ad-hoc plain-JDBC repositories elsewhere.

**Why:** Keeps domain mapping on JPA while giving the poller an explicit Spring Data JDBC boundary. Separate `@EnableJpaRepositories` / `@EnableJdbcRepositories` base packages avoid dual scanning.

## ADR-009: Full-stack Docker Compose for review demos

**Status:** Accepted

**Context:** Reviewers (and VPS-shaped demos) should exercise multi-tenancy without installing JDK/Node. Postgres-only Compose was not enough for a single-command demo.

**Decision:** Default `docker-compose.yml` runs **postgres**, **api** (multi-stage Maven image), **web** (Angular production build behind nginx with same-origin `/api` proxy), and **webhook-echo** (in-compose delivery target). Hybrid host runs remain documented (`compose up postgres` + local processes).

**Why:** Matches a realistic single-VPS layout. `SSRF_ALLOW_PRIVATE` is enabled only in Compose so Docker DNS / RFC1918 targets like `webhook-echo` work; production should keep private addresses denied.

## ADR-010: BuildKit Maven cache for API image rebuilds

**Status:** Accepted

**Context:** Full `./mvnw package` inside Docker re-downloaded dependencies on every source change, making local iteration slow.

**Decision:** API Dockerfile resolves dependencies in a pom-only layer, then packages after `COPY src`. Both Maven steps use `RUN --mount=type=cache,target=/root/.m2` so the local repository persists across builds.

**Why:** Code-only rebuilds reuse downloaded artifacts; pom changes still refresh the dependency set.

