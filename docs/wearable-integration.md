# Wearable integration — research notes (Sept 2026)

**Goal:** pull sleep / recovery / bodyweight / auto-logged workouts from a
wearable so the app can (a) auto-regulate the progression coach ("you slept 5h
and HRV is down — hold weight today"), (b) stop asking for bodyweight by hand,
(c) annotate each session with heart rate + calories.

**Status: research only. Not started. Bigger than any single phase of the
current rework** — new OAuth flow, encrypted token storage, a nightly poll, new
tables, and UI. Also depends on which watch the user actually wears (see
"Hardware reality check").

---

## The big catch: Fitbit's legacy Web API is being turned down *now*

- The **legacy Fitbit Web API is deprecated in September 2026** and stops
  serving data. Do **not** build anything new on `api.fitbit.com`.
  ([announcement](https://community.fitbit.com/t5/Web-API-Development/Introducing-the-next-phase-of-the-Fitbit-Web-API/td-p/5821061),
  [dev.fitbit.com](https://dev.fitbit.com/build/reference/web-api/))
- The replacement is the **Google Health API** (`developers.google.com/health`),
  Google OAuth 2.0, a restructured model: ~120 legacy endpoints collapse into
  **~31 data types, 4 methods, one resource shape**, plus a "Reconciled Stream"
  that merges overlapping sources.
  ([Google](https://developers.google.com/health/about),
  [Terra overview](https://tryterra.co/blog/everything-you-need-to-know-about-google-health-new-api),
  [Sahha migration notes](https://sahha.ai/blog/fitbit-api-sunset-migration/))
- Migration is **not silent** — every user must re-consent through Google OAuth;
  old Fitbit tokens don't carry over.

So the target is the **Google Health API**, not the Fitbit Web API.

### Google Health API — what's available

| Available now (read) | Planned Q2–Q3 2026 (read + write) |
| --- | --- |
| Steps, calories / energy | Activity & fitness — 175+ activity types, goals, lifetime stats |
| Vitals (heart rate, blood pressure) | ECG, blood glucose, body temperature |
| Workouts (sessions: start/end, type, calories, HR) | Heart-rate variability, basal metabolic rate |
| Sleep & recovery | Mindfulness, expanded women's health |
| Nutrition & hydration | |
| Women's health | |

Sources: Fitbit devices + Google Pixel Watches (current and previous models).

### Access requirements (the friction)

- **All Google Health API scopes are "Restricted"** → a privacy & security
  review (CASA-style) is required to **publish** an app to arbitrary users.
- **For our case that's avoidable:** keep the Google Cloud OAuth consent screen
  in **"Testing"** and add the owner's own Google account as a test user.
  Restricted scopes work for test users with no security assessment — you just
  get a scarier consent screen and a ~100-user cap. Fine for a personal app;
  the moment you'd want to share it, the review kicks in.
- Rate limits aren't published yet; the legacy API was **150 requests/hour per
  consented user**, and a nightly poll is nowhere near that.
  ([legacy rate limits](https://dev.fitbit.com/build/reference/web-api/developer-guide/application-design/))

### Fallback if the user isn't on Fitbit/Pixel — see hardware check below

- **Apple Watch:** no server-side API. HealthKit is on-device only; you'd need a
  companion iOS app that reads HealthKit and pushes to gym-api. Much bigger.
- **Garmin:** Garmin Health API / Connect Developer Program — requires partner
  approval, but has a proper server-to-server push (webhooks). Viable but
  gated.
- **Whoop / Oura:** clean REST APIs, OAuth 2.0, generous free tiers, great
  recovery/sleep data — if the user happens to wear one, this is the *easiest*
  path of all.
- **Aggregators (Terra, Sahha, Rook, Spike):** one API, one OAuth, they handle
  Fitbit/Garmin/Oura/Whoop/Apple/Google behind it. Costs money past a free
  tier, but removes all of the above pain. Worth it if this becomes real.

---

## Hardware reality check (answer before doing anything)

1. **What does the user wear to the gym** — Fitbit, Pixel Watch, Apple Watch,
   Garmin, Whoop, Oura, nothing? This picks the entire integration path.
2. Do they actually **log bodyweight / food** in that ecosystem, or just wear
   the watch? (Determines whether nutrition features are worth it.)
3. Do they want the watch to **drive** training decisions (auto-regulation) or
   just **decorate** the history view? (Determines scope.)

---

## What we could build (ranked by value ÷ effort)

### 1. Recovery-aware progression coach ★ (the reason to do this)

Each morning pull **last night's sleep** (total + stages), **resting heart
rate**, and **HRV**. Compute a simple readiness score (deviation from the user's
own 30-day baseline). Feed it into `GET /api/program/next`:

- readiness good → coach behaves as today
- readiness poor (short sleep, RHR up, HRV down) → clamp every suggestion to at
  most "hold weight"; surface *"Rough night — treat today as maintenance."*
- readiness great for several days → allow a slightly bigger jump

Backend: `ProgressionService.suggest(...)` gains an optional `readiness`
modifier; `next()` attaches a `readiness` block to the response.
Frontend: a "Recovery" card at the top of **Today**.

### 2. Bodyweight auto-sync ★

Pull the daily weight (and body-fat %) reading. Store a `body_weight_log` table.
Uses: relative-strength trend on **Progress** (e1RM ÷ bodyweight), and a
"bodyweight" line the user never has to type. Low effort, always-on value.

### 3. Session enrichment ★

Fitbit/Google logs a "Weights"/"Workout" activity with start/end, duration,
calories, average & peak HR, Active Zone Minutes. On the day of a gym-tracker
workout, attach that to the workout: calories, HR zones, duration, and a
sanity check ("you logged a Push day but the watch saw no workout").

Store `external_workout` keyed by owner + date; join it in `GET /api/workouts/{id}`.

### 4. Intra-workout heart rate (needs intraday / higher-res scope)

Overlay minute-level HR on the session timeline. Derive: real rest taken
between sets (HR recovery), a session-RPE estimate, time in each zone. Nice on
the **Exercise/Workout detail** page. Higher effort, needs the granular scope.

### 5. Life-context on Progress

Show daily **steps** and **active minutes** next to lifting volume, so a bad
session lines up with a 15k-step travel day or a bad-sleep week. Pure read,
pure decoration, cheap once #1's plumbing exists.

### 6. Cardio-fitness (VO2max) trend

A long-term conditioning line beside strength PRs. Cheap, mildly interesting.

### 7. Nutrition vs training (low priority)

If the user logs food: protein & calories on training vs rest days, with a
gentle "you're under-eating to progress" flag. Only worth it if they already
log food — most people don't.

---

## Rough architecture when we do it

```
Settings → "Connect wearable"  (browser OAuth 2.0 + PKCE against Google;
                                 avoids routing Google's redirect through
                                 Cloudflare Access — the SPA does the dance and
                                 hands gym-api the refresh token once)
        │
gym-api  health/  module
  - health_connection   per-owner: provider, encrypted refresh token, scopes, last sync
  - daily_readiness     date, sleepMinutes, sleepStages, restingHr, hrv, score
  - body_weight_log     date, weightKg, bodyFatPct, source
  - external_workout    date, provider activity id, type, start, end, calories, avgHr, peakHr, azm
        │
  nightly poll (Spring @Scheduled or a k8s CronJob hitting an internal endpoint)
  → refresh token, pull the last ~3 days of sleep/RHR/HRV/weight/workouts, upsert
        │
  ProgressionService gains an optional readiness modifier
  GET /api/program/next adds a `readiness` block
  GET /api/workouts/{id} joins `external_workout`
```

### Auth wrinkles

- The **OAuth redirect URI must be publicly reachable**. `gym.chrisvds.com`
  is behind Cloudflare Access, which will hijack Google's callback. Two ways
  out: (a) an Access **bypass** policy scoped to exactly the callback path, or
  (b) do the whole OAuth flow in the browser with **PKCE** (public client) and
  POST just the resulting refresh token to gym-api. (b) is cleaner and keeps
  Access untouched.
- The refresh token is a real secret. Encrypt it at rest (a
  `HEALTH_TOKEN_KEY` env from a k8s Secret, AES-GCM in the `health` module) —
  don't store it plaintext in Postgres.
- Google Cloud project + OAuth consent screen in **Testing**, owner added as a
  test user. Restricted scopes: `.../auth/health.sleep.read`,
  `health.heart_rate.read`, `health.activity.read` (names per the Health API
  docs at build time).

### Open questions to resolve first

- Is the Google Health API's **"activity & fitness" read** actually GA yet, or
  still in the Q2–Q3 2026 "planned" bucket? (Sleep/vitals/workouts are live;
  the strength-training-relevant bits were still rolling out at time of writing.)
- Current rate limits / quota for the Health API.
- Whether "workout session" granularity distinguishes a lifting session well
  enough to be useful for #3.

---

## Sources

- [Fitbit — "next phase of the Web API" announcement](https://community.fitbit.com/t5/Web-API-Development/Introducing-the-next-phase-of-the-Fitbit-Web-API/td-p/5821061)
- [Fitbit Web API reference / deprecation notice](https://dev.fitbit.com/build/reference/web-api/)
- [Fitbit Web API developer guide — rate limits & app types](https://dev.fitbit.com/build/reference/web-api/developer-guide/application-design/)
- [Google Health API — about](https://developers.google.com/health/about)
- [Terra — everything about the new Google Health API](https://tryterra.co/blog/everything-you-need-to-know-about-google-health-new-api)
- [Sahha — Fitbit Web API sunset & migration](https://sahha.ai/blog/fitbit-api-sunset-migration/)
- [Sahha — Google Fit API sunset (context: Google Fit also dies end of 2026)](https://sahha.ai/blog/google-fit-api-sunset-migration/)
- [Thryve — Fitbit API deprecation analysis](https://www.thryve.health/blog/fitbit-api-deprecation)
