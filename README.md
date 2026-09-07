# gym-tracker-backend (`gym-api`)

Backend API for [`gym-tracker`](https://github.com/chrisvlds/gym-tracker).
**Spring Boot 4 / Java 21.** Deployed image: `ghcr.io/chrisvlds/gym-api`.

Right now it's a skeleton — health endpoints, CORS, and a `/api/ping`. The real
job (cross-device workout sync + backup for the localStorage-only frontend) is
still to be designed.

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
  config/WebConfig.java      CORS for the SPA origin
src/main/resources/
  application.properties         base config (port, actuator)
  application-prod.properties    prod CORS origin (https://gym.chrisvds.com)
Dockerfile                       multi-stage: temurin-jdk build -> temurin-jre run
```

The `prod` profile (`SPRING_PROFILES_ACTIVE=prod`, set by the k8s Deployment)
locks CORS to `https://gym.chrisvds.com`. Override with
`GYM_API_CORS_ALLOWED_ORIGINS` (comma-separated).

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

Public URL (`gym-api.chrisvds.com`) is added as a Public Hostname on the
Cloudflare tunnel — see `gym-tracker-infra/docs/cloudflare-tunnel.md`.
