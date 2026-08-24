# 🚛 FleetTrack — Vehicle Fleet Management System

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Redis-7-red?logo=redis&logoColor=white" alt="Redis"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" alt="License"/>
</p>

<p align="center">
  A monolithic Spring Boot backend for managing vehicle fleets — registration, driver profiles, real-time GPS tracking, maintenance history, and role-based access, built with production-grade patterns.
</p>

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [🐳 Running with Docker](#-running-with-docker)
- [Running Locally (without Docker)](#-running-locally-without-docker)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
- [Authentication & Roles](#-authentication--roles)
- [WebSocket — Live Location Tracking](#-websocket--live-location-tracking)
- [PDF Reports](#-pdf-reports)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [License](#-license)

---

## ✨ Features

- 🚗 **Vehicle management** — full CRUD with make, model, year, license plate, VIN, status, odometer
- 👤 **Driver profile management** — licenses, contact info, active status, vehicle assignment
- 📍 **Real-time GPS tracking** — WebSocket (STOMP) broadcast of live vehicle coordinates
- 🔐 **JWT authentication** — stateless auth with `ADMIN` / `FLEET_MANAGER` roles
- 🔍 **Dynamic filtering, sorting & pagination** — via JPA Specifications
- ⚡ **Redis caching** — TTL-based caching for vehicle/driver summaries
- 📢 **Redis Pub/Sub notifications** — real-time maintenance & status alerts
- ⏰ **Scheduled maintenance alerts** — daily cron job flags upcoming/overdue service
- 🗄️ **Flyway migrations** — version-controlled schema evolution
- 📄 **PDF report generation** — fleet status, maintenance logs, driver activity
- 🛡️ **Rate limiting** — per-IP token bucket (Bucket4j)
- 📚 **Swagger / OpenAPI** — interactive API docs
- ✅ **Global exception handling** — consistent `ApiResponse` / `ApiErrorResponse` contract
- 🧪 **Unit & integration tests** — Mockito for services, `@WebMvcTest` for controllers, Testcontainers for full-stack flows

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.2 |
| Security | Spring Security + JJWT |
| Persistence | Spring Data JPA, PostgreSQL 16 |
| Migrations | Flyway |
| Caching / Messaging | Redis (cache + pub/sub) |
| Real-time | Spring WebSocket (STOMP) |
| Mapping | MapStruct |
| Docs | springdoc-openapi (Swagger UI) |
| PDF | iText7 |
| Rate limiting | Bucket4j |
| Build | Gradle 8.11.1 |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## 🚀 Getting Started

### Prerequisites

- 🐳 [Docker](https://www.docker.com/products/docker-desktop/) & Docker Compose (recommended path)
- ☕ JDK 17 (only needed for running outside Docker)
- 🐘 PostgreSQL 16 (only needed for running outside Docker)
- 🟥 Redis 7 (only needed for running outside Docker)

### Clone the repository

```bash
git clone https://github.com/elsen111/fleet_track.git
cd fleet_track/fleettrack
```

---

## 🐳 Running with Docker

This is the recommended way to run FleetTrack — it spins up the app, PostgreSQL, and Redis together with a single command.

### 1. Set up environment variables

Copy the example file and fill in real values:

```bash
cp .env.example .env
```

At minimum, set:

```env
POSTGRES_USER=fleettrack
POSTGRES_PASSWORD=your-strong-password
POSTGRES_DB=fleettrack

DB_HOST=postgres
DB_PORT=5432
DB_NAME=fleettrack
DB_USER=fleettrack
DB_PASSWORD=your-strong-password

REDIS_HOST=redis
REDIS_PORT=6379

JWT_SECRET=your-base64-encoded-secret
CORS_ORIGINS=http://localhost:3000
```

> ⚠️ **Never commit `.env`.** It's already excluded via `.gitignore` — only `.env.example` should be tracked.

### 2. Build and start the stack

```bash
docker compose build
docker compose up
```

Or in one step, detached:

```bash
docker compose up -d --build
```

This starts three containers:

| Container | Purpose | Port |
|---|---|---|
| `fleettrack-app` | Spring Boot application | `8080` |
| `fleettrack-postgres` | PostgreSQL database | `5432` |
| `fleettrack-redis` | Redis cache & pub/sub | `6379` |

### 3. Verify it's running

```bash
docker compose ps
```

All three services should show as `running` (Postgres and Redis as `healthy`). Then check:

```
http://localhost:8080/swagger-ui.html
```

### 4. View logs

```bash
docker compose logs -f app
```

### 5. Stop the stack

```bash
docker compose down
```

To also wipe the database volume (clean slate):

```bash
docker compose down -v
```

### 6. Rebuild after code changes

```bash
docker compose build --no-cache app
docker compose up
```

---

## 💻 Running Locally (without Docker)

1. Start PostgreSQL and Redis locally, or point to remote instances.
2. Export environment variables (or rely on the defaults in `application.yaml`):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=fleettrack
export DB_USER=fleettrack
export DB_PASSWORD=fleettrack
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-base64-encoded-secret
```

3. Run with Gradle:

```bash
./gradlew bootRun
```

4. The API will be available at `http://localhost:8080`.

---

## 🔧 Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `fleettrack` |
| `DB_USER` | Database user | `fleettrack` |
| `DB_PASSWORD` | Database password | `fleettrack` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | *(empty)* |
| `JWT_SECRET` | Base64-encoded JWT signing key | dev placeholder — **override in real use** |
| `CORS_ORIGINS` | Allowed CORS origin(s) | `http://localhost:3000` |
| `SERVER_PORT` | App port | `8080` |

---

## 📚 API Documentation

Once running, interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

### Core endpoint groups

| Group | Base path |
|---|---|
| Auth | `/api/auth` |
| Vehicles | `/api/vehicles` |
| Drivers | `/api/drivers` |
| Maintenance records | `/api/maintenance-records` |
| Reports | `/api/reports` |
| WebSocket | `/ws` |

---

## 🔐 Authentication & Roles

FleetTrack uses **stateless JWT authentication** with two roles:

- 🛡️ `ADMIN` — full access, including deletes
- 👷 `FLEET_MANAGER` — create/update access; shared visibility across the whole fleet

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@fleettrack.local",
    "password": "StrongPass123!",
    "fullName": "Fleet Admin",
    "role": "ADMIN"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@fleettrack.local",
    "password": "StrongPass123!"
  }'
```

Use the returned `accessToken` on subsequent requests:

```bash
curl http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer <accessToken>"
```

Access tokens expire in 15 minutes; use `/api/auth/refresh` with the `refreshToken` (valid 7 days) to get a new pair.

---

## 📡 WebSocket — Live Location Tracking

FleetTrack streams live GPS updates over STOMP-over-SockJS.

- **Connect:** `ws://localhost:8080/ws`
- **Send a location ping:** `/app/location.update`
- **Subscribe to one vehicle:** `/topic/vehicles/{vehicleId}/location`
- **Subscribe to the whole fleet:** `/topic/vehicles/location`
- **Subscribe to notifications:** `/topic/notifications`

---

## 📄 PDF Reports

All report endpoints require `ADMIN` or `FLEET_MANAGER` role and return a downloadable PDF:

| Endpoint | Description |
|---|---|
| `GET /api/reports/fleet-status` | Full fleet status summary |
| `GET /api/reports/maintenance-log/{vehicleId}` | Maintenance history for a vehicle |
| `GET /api/reports/driver-activity` | Driver roster with assignment status |

---

## 🧪 Testing

Run the full test suite:

```bash
./gradlew test
```

- **Unit tests** — Mockito, service layer logic (validation rules, business rules, exception paths)
- **Controller tests** — `@WebMvcTest`, request/response contract validation
- **Integration tests** — Testcontainers spins up real PostgreSQL + Redis instances for end-to-end flows

---

## 📁 Project Structure

```
com.fleet_track
├── config/          # Security, Redis, WebSocket, OpenAPI, scheduling config
├── controller/       # REST controllers
├── dto/
│   ├── request/      # Inbound request DTOs
│   └── response/      # Outbound response DTOs
├── entity/          # JPA entities
├── enums/            # Domain enums
├── exception/        # Custom exceptions + global handler
├── filter/            # Rate limiting filter
├── mapper/            # MapStruct DTO ↔ entity mappers
├── repository/       # Spring Data repositories
│   └── specification/ # JPA Specifications for filtering
├── scheduler/         # Scheduled jobs
├── security/          # JWT, UserDetails, filters
├── service/           # Service interfaces
│   └── impl/          # Service implementations
└── util/               # Shared utilities
```
