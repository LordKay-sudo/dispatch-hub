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