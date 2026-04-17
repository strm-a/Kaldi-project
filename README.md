# Kaldi Support API

Quarkus backend for a customer-support chat, secured with Keycloak (OIDC).

## Prerequisites

- Docker + Docker Compose
- (Optional) [Bruno](https://www.usebruno.com/) to run the prepared API calls

## One-command startup

```bash
docker compose up --build
```

That's it. Compose builds the API image, boots Postgres, Keycloak (with the
`kaldi` realm auto-imported), and the API. First boot takes a minute or two
while Maven pulls dependencies; subsequent boots are fast.

Ports:

| Service  | URL                                       |
|----------|-------------------------------------------|
| API      | http://localhost:8080                     |
| Swagger  | http://localhost:8080/swagger-ui          |
| Keycloak | http://localhost:8081 (admin / admin)     |

## Credentials

| Who      | Username        | Password | Role       | Where                      |
|----------|-----------------|----------|------------|----------------------------|
| Operator | `aliceOperator` | `alice`  | `operator` | Keycloak realm `kaldi`     |
| Operator | `mikeOperator`  | `mike`   | `operator` | Keycloak realm `kaldi`     |
| Operator | `lucyOperator`  | `lucy`   | `operator` | Keycloak realm `kaldi`     |

The app's Postgres is pre-seeded with 3 users (`ana`, `john`, `maja`),
3 operators (`mikeOperator`, `lucyOperator`, `aliceOperator`), and 4 chats — see
[postgres/seed.sql](postgres/seed.sql).

## Documentation

- Static endpoint docs: [docs/README.md](docs/README.md)
- Live Swagger UI: http://localhost:8080/swagger-ui

The `docs/` folder can be read without starting the project. Swagger UI is only
available while the API is running and should be used as generated reference,
not as the primary testing tool.

## Testing with [Bruno](https://www.usebruno.com/)

1. Open Bruno → **Open Collection** → select the `bruno/` folder
2. Pick the `local` environment (top-right)
3. Run **Auth / Login as aliceOperator** — this saves an access token into `{{token}}`
4. Run any request under **User/** or **Operator/** — tokens are auto-attached

Bruno collection layout:

- `Auth/Login as aliceOperator` (or any other operators) — OIDC password grant, stores `token` + `refreshToken`
- `Auth/Refresh token` — exchanges `refreshToken` for a fresh access token
- `User/*` — anonymous endpoints (start chat, send message, get messages)
- `Operator/*` — protected endpoints (list / acquire / message / get messages)

## Swagger UI

Open http://localhost:8080/swagger-ui when the project is running.

Use Swagger UI to inspect endpoints, request bodies, response schemas, and the
generated OpenAPI definition. For actually testing requests, use the Bruno
collection above or another HTTP client.

## Running the test suite

```bash
./mvnw test
```

Tests use H2 in-memory and mock OIDC via `@TestSecurity`, so they run with
neither Postgres nor Keycloak.
