# gym-tracker-backend (`gym-api`)

Backend API for [`gym-tracker`](https://github.com/chrisvlds/gym-tracker).
**Spring Boot 4 / Java 21.** Deployed image: `ghcr.io/chrisvlds/gym-api`.

Whole-blob backup for the localStorage-only frontend: the SPA pushes its entire
state document up and pulls it back on another device. **Last write wins** — no
merge/conflict handling, and a single document (`id = "default"`) until there's
per-user auth.

## Status (2026-09-08)

- ✅ `GET`/`PUT /api/state` live in the cluster, backed by the `postgres`
  StatefulSet (`gym-tracker` namespace). Verified end to end via
  `https://gym.chrisvds.com` — a row exists in `workout_state`.
- ✅ Deploys itself: push to `develop` → image → dispatch → `gym-tracker-infra`
  pins the tag → Flux rolls it.
- `/api/*` is gated by the **Cloudflare Access** policy on `gym.chrisvds.com`
  (email login). The service itself has no auth.
- ⬜ Next ideas: a real domain model instead of one opaque blob, per-user rows,
  Flyway migrations. See `gym-tracker-infra/TODO.md`.

## Run locally

```bash
./mvnw spring-boot:run          # http://localhost:8080, H2 file DB in ./data/
./mvnw test                     # H2 in-memory
```

| Endpoint | |
| --- | --- |
| `GET /api/ping` | `{ status, service, version, time }` |
| `GET /api/state` | `204` if nothing stored, else `{ updatedAt, payload }` |
| `PUT /api/state` | body = the state JSON; stores it; → `{ updatedAt }` (`413` if > 5 MB) |
| `GET /actuator/health/{liveness,readiness}` | k8s probes |
| `GET /actuator/info` | build info |

## Layout

```
src/main/java/com/chrisvds/gymapi/
  GymApiApplication.java          entrypoint
  web/PingController.java         /api/ping
  config/WebConfig.java           CORS (dev only — see below)
  state/WorkoutState.java         @Entity: id, payload (text), updatedAt
  state/WorkoutStateRepository.java
  state/StateController.java      GET/PUT /api/state
src/main/resources/
  application.properties          base + H2 datasource (dev/test default)
  application-prod.properties     Postgres via SPRING_DATASOURCE_* env
Dockerfile                        multi-stage: temurin-jdk build -> temurin-jre run
```

**Database:** H2 file DB locally; **Postgres** in the cluster (the `postgres`
StatefulSet in `gym-tracker-infra`, same namespace). Schema is managed by
Hibernate `ddl-auto=update` — fine for the one-table schema; swap to Flyway if
it grows.

**CORS is a local-dev convenience.** In production the frontend's nginx
reverse-proxies `/api` to this service, so browser calls are same-origin and
CORS never applies. `WebConfig` defaults to `http://localhost:5173` (the Vite
dev server); override with `GYM_API_CORS_ALLOWED_ORIGINS` (comma-separated) if
needed.

## Deploying

This repo only **builds the image**. `.github/workflows/publish-image.yml`, on a
push to `develop`:

1. builds a `linux/arm64` image → `ghcr.io/chrisvlds/gym-api:sha-<commit>` (+ `:latest`)
2. fires `repository_dispatch: image-updated {image: "gym-api", tag}` at
   [`gym-tracker-infra`](https://github.com/chrisvlds/gym-tracker-infra)

That repo pins the tag in `deploy/apps/gym-api/` and Flux rolls it onto the Pi
(namespace `gym-tracker`, reached in-cluster at
`http://gym-api.gym-tracker.svc.cluster.local:8080`).

**Required repo secret:** `INFRA_DISPATCH_TOKEN` — fine-grained PAT scoped to
`gym-tracker-infra`, **Contents: Read and write** (same token pattern as the
`gym-tracker` repo).

Not exposed publicly — no ingress, no tunnel hostname. The `gym-tracker`
frontend's nginx reverse-proxies `/api/` to
`gym-api.gym-tracker.svc.cluster.local:8080`, so the browser reaches it
same-origin at `https://gym.chrisvds.com/api/*`.

**Auth:** none in the service itself. `gym.chrisvds.com` (and therefore
`/api/*`) is behind a **Cloudflare Access** policy — email login, allow-list of
one — so only an authenticated browser reaches it. Fine for a single user;
add real auth here if it's ever shared.
