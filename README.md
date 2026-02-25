# TournaX Platform (CS203)

> Full-stack tournament operations platform that pairs a Spring Boot API with a Next.js dashboard for admins, players, and spectators.

## Overview

TournaX centralizes tournament scheduling, player onboarding, match officiating, live statistics, and Elo tracking. The backend (`cs203system`) exposes secured REST + WebSocket services, while the frontend (`frontend`) delivers an authenticated dashboard experience powered by NextAuth, Tailwind, and a component library of reusable forms, tables, and charts.

## Repository Layout

| Path | Purpose |
| --- | --- |
| `cs203system/` | Spring Boot 3.3 service layer (JPA, MapStruct, H2/MySQL-ready persistence, JWT Security, STOMP notifications). |
| `frontend/` | Next.js 14 App Router project with NextAuth credentials provider, ShadCN UI, Tailwind, Zustand, and data fetching via Axios. |
| `frontend/src/app/test/page.tsx` | Minimal SockJS/STOMP client for validating `/ws` notifications without leaving the repo. |

## Core Capabilities

### Backend highlights

- **Tournament engines:** Swiss, double elimination, and hybrid brackets share the same orchestration contract (`TournamentFormatManager`) so the service can validate player counts, seed rounds, and advance winners consistently.
- **Player lifecycle:** Admin and player entities extend `User`, which implements `UserDetails` for seamless Spring Security integration. Player-specific aggregates (loss counts, Elo, Swiss points) are persisted and surfaced via DTO mappers.
- **Match + stats tracking:** Matches capture scores, brackets, KO outcomes, punches, and dodges. `PlayerStatsService` aggregates those micro-stats into DTOs for leaderboards and dashboards.
- **Elo history:** Every rating change is recorded through `EloRecordService`, enabling historical charts per player.
- **Security:** Stateless JWT auth with RSA keypairs generated on boot, role hierarchy (`ROLE_ADMIN > ROLE_PLAYER`), and fine-grained `HttpSecurity` matchers so public landing pages stay open while management APIs remain protected.
- **Observability & tooling:** H2 console, verbose SQL logging, Springdoc OpenAPI (`/swagger-ui/index.html` & `/api-docs`), and Jacoco coverage are enabled out of the box.
- **Realtime notifications:** WebSocket/STOMP endpoint (`/ws`) broadcasts lifecycle events (start, end, etc.) via `NotificationService` to `/user/{id}/notifications` destinations.

### Frontend highlights

- **NextAuth credential flow:** `/api/auth/[...nextauth]` proxies login requests to `POST /api/auth/login`, stores JWT payloads in the session, and hydrates React via `SessionProvider`.
- **Role-aware dashboards:** `src/app/dashboard` routes fan out to admin list views (tournaments, matches, users) or player-specific profiles after inspecting the authenticated session.
- **Rich UI kit:** ShadCN form primitives, Radix UI components, Recharts graphs, and custom widgets (`TournamentCard`, `MatchUpdateForm`, etc.) encapsulate complex flows such as seeding players, updating scorecards, and editing tournament metadata.
- **Data access:** A shared Axios instance injects the NextAuth JWT (client/server) and honors the `NEXT_PUBLIC_API_URL` base, so every component benefits from consistent headers and error handling.
- **Live experimentation sandbox:** `src/app/test/page.tsx` demonstrates how to subscribe to `/user/{id}/notifications` using SockJS + StompJS, mirroring the production notification flow.

## Prerequisites

- Java 17 (the Gradle toolchain enforces this version)
- Node.js 18+ and npm (or pnpm/yarn) for the Next.js app
- Docker (optional) if you want to build/run the backend container image

> **Tip:** The backend defaults to an in-memory H2 database. Switch to MySQL/PostgreSQL by uncommenting the datasource section in `cs203system/src/main/resources/application.properties` and providing valid credentials.

## Backend Setup (`cs203system`)

```bash
cd cs203system
# 1. Install dependencies & run migrations
./gradlew clean build        # Windows: gradlew.bat clean build

# 2. Start the API (hot reload friendly)
./gradlew bootRun            # or: java -jar build/libs/cs203system-0.0.1-SNAPSHOT.jar
```

- **Default ports:** HTTP on `http://localhost:8080`, H2 console on `/h2-console` (JDBC URL `jdbc:h2:mem:testdb`, user `sa`, password `1234`).
- **API explorer:** Swagger UI lives at `http://localhost:8080/swagger-ui/index.html`.
- **OpenAPI spec:** `http://localhost:8080/api-docs`.
- **WebSockets:** Connect to `ws://localhost:8080/ws` (SockJS fallback) and subscribe to `/user/{playerId}/notifications`.

### Database configuration example (MySQL)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tournax
spring.datasource.username=tournax
spring.datasource.password=change-me
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

Disable `spring.jpa.hibernate.ddl-auto=create-drop` in production environments to preserve data.

## Frontend Setup (`frontend`)

```bash
cd frontend
npm install

# Copy the sample config into .env.local if needed
cp .env.example .env.local  # optional helper if you add one

npm run dev
```

Create a `.env.local` with the API + NextAuth settings:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=replace-with-openssl-rand-base64-32
```

Visit `http://localhost:3000` — the root route redirects to `/login`. Use `/register/admin` or `/register/player` to create accounts, then explore the dashboard (`/dashboard`).

## Useful Commands

| Task | Command |
| --- | --- |
| Run API locally | `cd cs203system && ./gradlew bootRun` |
| Run backend tests + coverage | `cd cs203system && ./gradlew test jacocoTestReport` (HTML report in `build/jacocoHtml`) |
| Package backend Jar | `cd cs203system && ./gradlew bootJar` |
| Build backend container | `cd cs203system && docker build -t tournax-api .` |
| Start frontend dev server | `cd frontend && npm run dev` |
| Frontend production build | `cd frontend && npm run build && npm run start` |
| Lint frontend | `cd frontend && npm run lint` |

## API Surface (high level)

| Endpoint | Description |
| --- | --- |
| `POST /api/auth/register` / `POST /api/auth/login` | Create accounts and obtain JWT-backed sessions (consumed by NextAuth credentials provider). |
| `GET /api/tournament` | Public list of tournaments; admins can `POST`, `PUT`, `DELETE`, and `/start/{id}` to manage formats and lifecycle. |
| `PUT /api/tournament/{id}/players` | Add/remove players gated by Elo constraints. |
| `PUT /api/tournament/match` | Submit match outcomes (scores, punches/dodges, KO flags) and trigger automatic bracket progressions. |
| `GET /api/match/**`, `GET /api/player/**` | Public stats, rankings, and match histories. |
| `GET /api/player-stats` | Aggregated punches/dodges/KO metrics per player. |
| `GET /api/elo-records/**` | Elo timelines for analytics/visualizations. |

Use the Swagger UI for full request/response schemas — DTO mappers keep payloads lightweight for the frontend.

## Testing & Quality Gates

- **Backend:** `./gradlew test` runs the Spring Boot test suite; add `jacocoTestReport` for coverage artifacts. Mockito + Testcontainers-ready dependencies are already configured if you want to expand coverage.
- **Frontend:** `npm run lint` enforces Next.js + ESLint defaults. Add component/integration tests (e.g., Playwright, Vitest) as needed.
- **Manual verification:**
	- Hit `http://localhost:8080/swagger-ui/index.html` to ensure the API is up.
	- Load `http://localhost:3000/test` to confirm WebSocket notifications flow end-to-end.

## Deployment Notes

1. Build the backend jar (`./gradlew bootJar`) or Docker image (`docker build -t tournax-api .`).
2. Provide persistent storage and a managed database (MySQL/PostgreSQL). Update `application.properties` accordingly and disable `create-drop`.
3. For the frontend, run `npm run build` and host the `.next` output (Vercel, Azure Static Web Apps, etc.). Update `NEXT_PUBLIC_API_URL` to your deployed API origin and set `NEXTAUTH_URL/NEXTAUTH_SECRET` via environment variables.
4. Ensure CORS/WebSocket origins in `SecurityConfig` and `WebSocket` match your production hosts.

## Troubleshooting

- **401 from the API:** Confirm the frontend is sending the JWT (check the `Authorization` header). Log out/in to refresh expired tokens.
- **Players fail to join tournaments:** The backend enforces Elo min/max bounds and even player counts (and powers of two for double-elimination/hybrid). Adjust tournament settings or add more players.
- **WebSocket subscription fails:** Make sure `/ws` is reachable (not blocked by a proxy) and that the client subscribes to `/user/{id}/notifications` with a valid authenticated session.
- **H2 console login errors:** Use `jdbc:h2:mem:testdb`, username `sa`, password `1234`, and disable browser extensions that block frames (frame options are relaxed in `SecurityConfig`).
- **Port collisions:** Override `server.port` in `application.properties` or start the frontend on a different port via `npm run dev -- -p 4000`.

## Next Steps

- Wire up the placeholder public pages (`/announcement`, `/matches`, `/rankings`) to live data.
- Expand automated tests (backend services + frontend forms) to lock down the tournament lifecycle.
- Replace the temporary SockJS test harness with a polished in-app notification center.

