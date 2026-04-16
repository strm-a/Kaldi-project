# Kaldi Support API

Quarkus backend for a customer-support chat, secured with Keycloak (OIDC).

## Prerequisites

- Docker + Docker Compose
- (Optional) [Bruno](https://www.usebruno.com/) to run the prepared API calls

Nothing else — no local Java, Maven, Postgres or Keycloak install required.

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
| Postgres | localhost:5432 (kaldi / kaldi123)         |

## Credentials

| Who      | Username        | Password | Role       | Where                      |
|----------|-----------------|----------|------------|----------------------------|
| Operator | `aliceOperator` | `alice`  | `operator` | Keycloak realm `kaldi`     |
| Admin    | `admin`         | `admin`  | -          | Keycloak master realm      |

The app's Postgres is pre-seeded with 3 users (`ana`, `john`, `maja`),
3 operators (`mikeOperator`, `lucyOperator`, `aliceOperator`), and 4 chats — see
[postgres/seed.sql](postgres/seed.sql).

## Testing with Bruno (recommended)

1. Open Bruno → **Open Collection** → select the `bruno/` folder
2. Pick the `local` environment (top-right)
3. Run **Auth / Login as aliceOperator** — this saves an access token into `{{token}}`
4. Run any request under **User/** or **Operator/** — tokens are auto-attached

Bruno collection layout:

- `Auth/Login as aliceOperator` — OIDC password grant, stores `token` + `refreshToken`
- `Auth/Refresh token` — exchanges `refreshToken` for a fresh access token
- `User/*` — anonymous endpoints (start chat, send message, get messages)
- `Operator/*` — protected endpoints (list / acquire / message / get messages)

## Testing with Swagger UI

1. Open http://localhost:8080/swagger-ui
2. Grab a token (Bruno *Login as aliceOperator*, or curl below)
3. Click **Authorize**, paste the token, click **Authorize** → **Close**
4. Try any endpoint

Token via curl:

```bash
curl -s -X POST http://localhost:8081/realms/kaldi/protocol/openid-connect/token \
  -d grant_type=password -d username=aliceOperator -d password=alice \
  -d client_id=quarkus-app -d client_secret=secret | jq -r .access_token
```

## Running the test suite

```bash
./mvnw test
```

Tests use H2 in-memory and mock OIDC via `@TestSecurity`, so they run with
neither Postgres nor Keycloak.

## Running in dev mode (for development, not reviewing)

```bash
./mvnw quarkus:dev
```

Dev mode uses Quarkus Dev Services for Keycloak (requires Docker) and expects
you to run your own Postgres at `localhost:5432`. Schema + seed data come from
[src/main/resources/import.sql](src/main/resources/import.sql) (Hibernate
drops and recreates on every restart).

## Project layout

```
.
├── docker-compose.yml           # one-shot reviewer setup
├── Dockerfile                   # multi-stage build (no local Maven needed)
├── README.md
├── pom.xml
├── bruno/                       # Bruno API collection
├── keycloak/
│   └── kaldi-realm.json         # realm, roles, users, client (auto-imported)
├── postgres/
│   └── seed.sql                 # schema + seed data for prod/docker-compose
└── src/
    └── main/resources/import.sql  # dev-mode seed (Hibernate auto-runs)
```
