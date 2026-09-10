# gym-tracker-backend (`gym-api`)

Backend API for [`gym-tracker`](https://github.com/chrisvlds/gym-tracker).
**Spring Boot 4 / Java 21.** Deployed image: `ghcr.io/chrisvlds/gym-api`.

A REST domain model for the training log: workouts, exercises, a PPL program,
and a progression coach. The SPA reads and writes through these endpoints and
keeps a localStorage cache + write queue so logging still works with no signal
at the gym.

## Status (2026-09-10)

- REST resources: `/api/workouts`, `/api/exercises`, `/api/program`,
  `/api/settings`, plus `/api/program/next` (guidance) and
  `/api/exercises/{id}/history` (per-exercise stats).
- Every row is scoped to an `owner`, resolved from the
  `Cf-Access-Authenticated-User-Email` header that Cloudflare Access injects
  (falls back to `gym-api.default-owner` = `local` for dev). No password code.
- Optimistic concurrency on workout writes: send `expectedUpdatedAt`, get `409`
  if another device changed it first.
- `POST /api/import` migrates the old `gym-tracker:v1` localStorage blob.
- The old opaque `GET`/`PUT /api/state` blob store is **gone**.
- Schema is still Hibernate `ddl-auto=update`; Flyway is the next step if it
  keeps growing (see `gym-tracker-infra/TODO.md`).

## Run locally

```bash
./mvnw spring-boot:run          # http://localhost:8080, H2 file DB in ./data/
./mvnw test                     # H2 in-memory
```

On first request per owner the built-in exercise catalog and the default PPL
program are seeded automatically.

### Endpoints

| Endpoint | |
| --- | --- |
| `GET /api/ping` | `{ status, service, version, time }` |
| `GET /api/exercises?includeArchived=` | catalog (built-ins + custom) |
| `POST /api/exercises` | `{ id?, name, muscle, equipment }` → created exercise |
| `PATCH /api/exercises/{id}` | partial update |
| `DELETE /api/exercises/{id}` | hard-delete if unused, else archive |
| `GET /api/exercises/{id}/history` | `{ sessions[], personalRecord }` — e1RM/volume per session |
| `GET /api/workouts?from=&to=&limit=` | summaries (newest first) |
| `GET /api/workouts/{id}` | full workout with entries + sets |
| `POST /api/workouts` | `{ id, date, note?, programDayKey?, entries? }` — upsert by client id |
| `PATCH /api/workouts/{id}` | `{ date?, note?, programDayKey?, expectedUpdatedAt? }` (`409` on stale) |
| `PUT /api/workouts/{id}/entries` | `{ expectedUpdatedAt?, entries[] }` — replace the whole tree |
| `DELETE /api/workouts/{id}` | |
| `GET /api/program` | the program: `{ name, cycle[], days[] }` |
| `PUT /api/program` | `{ name?, cycle?, days? }` — replace |
| `GET /api/program/next` | suggested next day + per-exercise last performance and advice |
| `GET /api/settings` / `PUT /api/settings` | `{ unit, restSeconds }` |
| `POST /api/import?force=` | one-time migration of the v1 localStorage blob |
| `GET /actuator/health/{liveness,readiness}` | k8s probes |
| `GET /actuator/info` | build info |

## Layout

```
src/main/java/com/chrisvds/gymapi/
  GymApiApplication.java     entrypoint
  common/                    OwnedEntity base, Lifts math, exception → HTTP mapping
  identity/CurrentUser.java  resolves the owner from the Cloudflare Access header
  exercise/                  Exercise entity + catalog seed + controller
  workout/                   Workout / WorkoutEntry / WorkoutSet + controller
  program/                   Program / ProgramDay / ProgramSlot, PPL defaults, /next
  progression/               ProgressionService — the double-progression coach
  stats/                     GET /api/exercises/{id}/history
  settings/                  per-owner unit + rest timer default
  imports/                   POST /api/import (v1 blob)
  web/PingController.java     /api/ping
  config/WebConfig.java       CORS (dev only — see below)
src/main/resources/
  application.properties       base + H2 datasource (dev/test default) + default-owner
  application-prod.properties   Postgres via SPRING_DATASOURCE_* env
Dockerfile                     multi-stage: temurin-jdk build -> temurin-jre run
```

### Progression rule (`ProgressionService`)

Looks at the most recent session for an exercise (working sets = those marked
done, or all sets with reps if none are marked) against the slot's rep window
(default 6–8):

| Last session | Advice |
| --- | --- |
| any set ≥ `repMax + 2` (e.g. 10 when target 8) | add weight now (`+1 increment`) |
| every working set ≥ `repMax` | add weight |
| all working sets in range | hold weight, chase reps |
| more than half the sets below `repMin` | hold; deload ~10% if it repeats |

Increment defaults come from the exercise's `equipment` (machine/barbell 5,
isolation cable 2.5, …) and can be overridden per program slot.

**Database:** H2 file DB locally; **Postgres** in the cluster (the `postgres`
StatefulSet in `gym-tracker-infra`, same namespace) via `SPRING_DATASOURCE_*`.

**CORS is a local-dev convenience.** In production the frontend's nginx
reverse-proxies `/api` to this service, so browser calls are same-origin and
CORS never applies. `WebConfig` defaults to `http://localhost:5173` (the Vite
dev server); override with `GYM_API_CORS_ALLOWED_ORIGINS` if needed.

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
`gym-tracker-infra`, **Contents: Read and write**.

**Auth:** none in the service itself. `gym.chrisvds.com` (and therefore
`/api/*`, same hostname) is behind a **Cloudflare Access** policy — email login,
allow-list of one. The service reads the email it injects only to namespace
rows by `owner`. See `gym-tracker-infra/docs/cloudflare-access.md`.
