# RouteService

A Spring Boot microservice that computes and persists optimised waiter routes for a given store and list of stands.

## Description

RouteService exposes a single `POST /api/routes` endpoint. It validates the request API key, checks for duplicate entries, generates a `RouteSummary` (with a new UUID route ID), and persists the result to a local JSON file (`config/db.json`). All accepted API keys are stored in `config/api-keys.json`.

## Tech stack

| Concern           | Technology                                   |
| ----------------- | -------------------------------------------- |
| Language          | Java 17                                      |
| Framework         | Spring Boot 4.0.3 (Spring Web MVC)           |
| Validation        | Jakarta Bean Validation                      |
| JSON              | Jackson Databind                             |
| Boilerplate       | Lombok                                       |
| Build tool        | Gradle (Kotlin DSL)                          |

## Configuration

| File                   | Purpose                                     |
| ---------------------- | ------------------------------------------- |
| `config/api-keys.json` | JSON array of accepted API keys             |
| `config/db.json`       | Persisted routes (auto-created on first run)|

```json
// config/api-keys.json
["d1a92d47-...", "8a5f037e-...", "..."]
```

## Endpoint

| Method | URI          | Auth              |
| ------ | ------------ | ----------------- |
| POST   | `/api/routes`| `Authorization` header required |

### Request body

```json
{
  "storeId": "<uuid>",
  "standIdList": ["<uuid>", "<uuid>"]
}
```

### Responses

| Status | Meaning                         |
| ------ | ------------------------------- |
| 200    | Route created, returns `RouteSummary` |
| 400    | Validation failure (empty `standIdList`, malformed body) |
| 403    | Missing or invalid API key      |
| 409    | Same `storeId` + `standIdList` already exists |
| 500    | Unexpected server error         |

## Setup

```bash
# Build the project
./gradlew build

# Or on Windows
gradlew.bat build
```

## Running

```bash
# Run with Gradle (listens on http://localhost:8081)
./gradlew bootRun

# Or on Windows
gradlew.bat bootRun
```

The server listens on **`http://localhost:8081`** (configured in `src/main/resources/application.properties`).

## Project structure

```
RouteService/
├── config/
│   ├── api-keys.json        # Valid API keys
│   └── db.json              # Persisted route data
├── src/main/java/.../
│   ├── controller/
│   │   ├── RouteController.java        # POST /api/routes
│   │   └── GlobalExceptionHandler.java # 400/500 error mapping
│   ├── model/               # CreateRouteInput, RouteSummary, etc.
│   └── service/
│       └── RouteService.java           # Business logic + persistence
├── src/main/resources/
│   └── application.properties         # server.port=8081
├── postman_collection.json
└── build.gradle.kts
```
