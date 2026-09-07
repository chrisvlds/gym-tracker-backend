# gym-tracker-backend (`gym-api`)

Backend API for [`gym-tracker`](https://github.com/chrisvlds/gym-tracker).
**Spring Boot 4 / Java 21.** Deployed image: `ghcr.io/chrisvlds/gym-api`.

Right now it's a skeleton — health endpoints and a `/api/ping`. The real job
(cross-device workout sync + backup for the localStorage-only frontend) is still
to be designed.

## Run locally

```bash
./mvnw spring-boot:run          # http://localhost:8080
./mvnw test
```

- `GET /api/ping` → `{ status, service, version, time }`
- `GET /actuator/health` → overall health
- `GET /actuator/health/{liveness,readiness}` → k8s probe endpoints
- `GET /actuator/info` → build info

## Layout

```
src/main/java/com/chrisvds/gymapi/
  GymApiApplication.java     entrypoint
  web/PingController.java    /api/ping
  config/WebConfig.java      CORS (dev only — see below)
src/main/resources/
  application.properties         base config (port, actuator)
  application-prod.properties    prod CORS origins
Dockerfile                       multi-stage: temurin-jdk build -> temurin-jre run
```

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
