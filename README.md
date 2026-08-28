# Dispatch Hub

Multi-tenant notification / webhook dispatcher take-home (WIP).

## Stack

- **API:** Spring Boot 4.1.1 (`dispatch-hub-api`), Java 21 (highest JDK on this machine; start.spring.io also offers 25/26)
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

3. Backend (from `backend/`):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Frontend (from `frontend/`):

   ```bash
   npm install
   npm start
   ```

Actuator health: `http://localhost:8080/actuator/health` (once the API is up and security is configured for local use).
