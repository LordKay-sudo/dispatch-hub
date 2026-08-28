# Design decisions

Short records of choices that affect security, ops, or review. Implementation may land after the scaffold; the intended behaviour is fixed here.

## ADR-001: Outbound URL checks use global deny then tenant allowlist

**Status:** Accepted (to implement with destinations / dispatch)

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
