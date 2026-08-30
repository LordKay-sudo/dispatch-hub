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

1. `docker compose up -d` then start API and web.
2. Log in as `admin.acme` / `password` / `acme`.
3. Create destination → submit event → show job moving to SUCCESS (or FAILED/DEAD with retry).
4. Show AI summary on a failed/dead job (mock provider is fine).
5. Show VIEWER cannot create destinations; cross-tenant access forbidden.
6. Show Swagger UI and `./mvnw verify` (or CI green on the `submission` tag).
7. Walk architecture: JWT tenant scope → outbox claim → SSRF → metrics.
