# FIL-ROUGE-25-26

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Backend](#backend)
- [Frontend](#frontend)
- [Authentication & Authorization](#authentication--authorization)
- [Database](#database)
- [API Reference](#api-reference)
- [Running the App](#running-the-app)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Project Layout](#project-layout)
- [Contribution Notes](#contribution-notes)

---

## Project Overview

`FIL-ROUGE-25-26` is a simple real estate management application built with:

- Java backend using `com.sun.net.httpserver.HttpServer`
- SQLite database for persistence
- Vanilla JavaScript frontend served as a static single-page app
- Token-based authentication with Bearer tokens

The project demonstrates a complete local full-stack setup with API routes, auth, protected resource access, and a small automation launcher.

## Architecture

The repository is organized into three main parts:

- `backend/` - Java source code, controllers, repositories, data models, and utility classes
- `frontend/` - static assets and client-side SPA logic
- `start-all.sh` - convenience script to compile, run backend, serve frontend, and seed test data

---

## Getting Started

### Prerequisites

- Java 17+ or compatible JDK
- Python 3 for the frontend static server
- `sqlite3` or support for SQLite in Java via `backend/lib/sqlite-jdbc.jar`
- `curl` for health checks in `start-all.sh`

### Recommended

- Use Linux or WSL for shell compatibility
- Install `lsof` or `ss` for port detection

---

## Backend

### Overview

The backend is implemented in `backend/Main.java` and registers HTTP routes for entity CRUD operations.

Key concepts:

- Controllers handle request parsing and response building
- Repositories handle SQLite persistence and schema creation
- `HttpUtils` adds CORS support, JSON handling, and token extraction
- `AuthService` manages in-memory token issuance and validation
- `PasswordUtils` hashes client-side passwords and verifies them during login

### Startup

The backend can be started manually from the repository root:

```bash
cd /home/rileya/work/ynov/B2/filRouge/FIL-ROUGE-25-26
java -cp backend/classes:backend/lib/sqlite-jdbc.jar Main
```

### Supported routes

Use the path prefix `/api` for all backend routes.

#### Agents

- `GET /api/agents`
- `GET /api/agents/{id}`
- `POST /api/agents`
- `PUT /api/agents/{id}`
- `DELETE /api/agents/{id}`

#### Clients

- `GET /api/clients`
- `GET /api/clients/{id}`
- `POST /api/clients`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

#### Addresses

- `GET /api/addresses`
- `GET /api/addresses/{id}`
- `POST /api/addresses`
- `PUT /api/addresses/{id}`
- `DELETE /api/addresses/{id}`

#### Properties

- `GET /api/properties`
- `GET /api/properties/{id}`
- `POST /api/properties`
- `PUT /api/properties/{id}`
- `DELETE /api/properties/{id}`

#### Property Images

- `GET /api/property_images`
- `GET /api/property_images/{id}`
- `POST /api/property_images`
- `PUT /api/property_images/{id}`
- `DELETE /api/property_images/{id}`

#### Favorites

- `GET /api/favorites`
- `GET /api/favorites/{id}`
- `POST /api/favorites`
- `PUT /api/favorites/{id}`
- `DELETE /api/favorites/{id}`

#### Contracts

- `GET /api/contracts`
- `GET /api/contracts/{id}`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `DELETE /api/contracts/{id}`

#### Transactions

- `GET /api/transactions`
- `GET /api/transactions/{id}`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

#### Visits

- `GET /api/visits`
- `GET /api/visits/{id}`
- `POST /api/visits`
- `PUT /api/visits/{id}`
- `DELETE /api/visits/{id}`

---

## Frontend

The frontend is a lightweight SPA powered by `frontend/app.js` and served as static HTML.

### How it works

- User authentication is performed by hashing the password in the browser and sending a login request to `/api/auth`
- The backend returns a Bearer token on successful login
- All protected calls include `Authorization: Bearer <token>`
- The SPA uses hash routing and dynamic UI state

### Running the frontend

From the repository root:

```bash
cd frontend
python3 -m http.server 5500
```

If `5500` is unavailable, the launcher script will choose an available port in the `5500-5510` range.

---

## Authentication & Authorization

### Login

- POST `/api/auth`
- Body JSON: `{ "email": "...", "password": "..." }`

The frontend performs client-side hashing before sending credentials.

### Tokens

- Tokens are issued in memory by `AuthService`
- Protected backend endpoints require `Authorization: Bearer <token>`
- The stored token is used to identify the current agent and enforce permissions

### Roles

- Admin agents can manage all resources
- Regular agents can access their own properties and perform limited updates

Default seeded credentials:

- Admin: `admin@ymmo.com` / `admin123`
- Agent: `agent1@ymmo.com` / `agent123`

---

## Database

The project uses a local SQLite database file at `filrouge.db`.

### Schema

- Created automatically at startup via `backend/db/Database.java`
- Tables include `agents`, `clients`, `addresses`, `properties`, `property_images`, `favorites`, `contracts`, `transactions`, `visits`
- Migrations are handled via schema checks such as `ensureColumnExists()`

### Password storage

- Passwords are stored as salted SHA-256 hashes
- The client-side password hashing flow produces a consistent `salt$hash` string

### Seed data

`start-all.sh` seeds the database with:

- admin and agent accounts
- one sample property and address
- one sample client

---

## Running the App

### One-step launcher

From the repository root, run:

```bash
./start-all.sh
```

This script will:

- compile backend sources
- launch the backend on `http://localhost:8080`
- create or reuse `filrouge.db`
- seed test data if needed
- start the frontend static server on the first free port from `5500` to `5510`

To stop the application:

```bash
./start-all.sh stop
```

---

## Testing

Existing backend test runner:

```bash
cd backend
./run-tests.sh
```

The test runner should compile and execute backend integration tests.

---

## Troubleshooting

### Port conflicts

- Backend uses port `8080`
- Frontend uses port `5500` by default
- If a port is already in use, stop the occupying process or restart with `./start-all.sh` after freeing the port

### JDBC driver issues

- Ensure `backend/lib/sqlite-jdbc.jar` exists
- The launcher script adds it to the runtime classpath

### Database issues

- Delete `filrouge.db` to reset the local database
- If schema migration fails, inspect `backend.log`

### CORS and authorization

- The backend exposes `OPTIONS` for CORS preflight
- Protected requests require `Authorization: Bearer <token>`

---

## Project Layout

- `backend/`
  - `Main.java` - app entry point and routing
  - `controller/` - HTTP request handlers
  - `db/` - SQLite repositories and database helpers
  - `model/` - data entities and enums
  - `util/` - helpers for HTTP, JSON, auth, password handling
- `frontend/`
  - SPA code and static UI assets
- `start-all.sh` - launcher script for backend, frontend, and test data seeding
- `filrouge.db` - local SQLite file (ignored by Git)

---

## Contribution Notes

- Keep API routes REST-like and consistent with `/api/*`
- Preserve backend token validation across protected controllers
- Use `backend/lib/sqlite-jdbc.jar` for local SQLite support
- Update `README.md` when adding new endpoints, auth behavior, or test instructions

---

## Additional Resources

- `backend/util/HttpUtils.java` for request/response utilities
- `backend/util/AuthService.java` for token lifecycle management
- `backend/util/PasswordUtils.java` for hashing and verification
- `backend/db/Database.java` for schema initialization and migration
- `frontend/app.js` for client login and protected API access
