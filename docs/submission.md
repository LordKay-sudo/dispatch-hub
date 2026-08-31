# Submission notes

## Tag

```bash
git fetch --tags
git checkout submission
```

Or open the `submission` release/tag on GitHub.

## Make the repository public (when handing in)

Repo starts private. When ready for reviewers:

```bash
gh repo edit LordKay-sudo/dispatch-hub --visibility public
```

Confirm with the recruiting contact that the URL is reachable.

## Demo script (5–10 minutes)

1. `cp .env.example .env` then `docker compose up --build`.
2. Open http://localhost:8080 — log in as `admin.acme` / `password` / `acme`.
3. Create destination `http://webhook-echo:5678/` → submit event → show job SUCCESS.
4. Show AI summary on a failed/dead job (mock provider is fine; force a bad URL or kill echo to demo failure).
5. Show VIEWER cannot create destinations; cross-tenant access forbidden (switch to `admin.beta`).
6. Show Swagger at http://localhost:8080/swagger-ui.html and CI green on the `submission` tag.
7. Walk architecture: JWT tenant scope → outbox claim → SSRF → metrics.

Hybrid alternative (no full image build): `docker compose up -d postgres`, then run API/web on the host (see README).
