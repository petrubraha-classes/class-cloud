# StoreService

A Spring Boot microservice for creating and persisting store entries with full metadata (location, operating hours, image).

## Description

StoreService exposes a single `POST /api/stores` endpoint. It validates the request API key, checks for duplicate store names per brand, creates a `StoreSummary` (with a new UUID store ID and timestamps), and persists the result to a local JSON file (`config/db.json`). All accepted API keys are stored in `config/api-keys.json`.

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

| File                   | Purpose                                      |
| ---------------------- | -------------------------------------------- |
| `config/api-keys.json` | JSON array of accepted API keys              |
| `config/db.json`       | Persisted stores (auto-created on first run) |

```json
// config/api-keys.json
["d1a92d47-...", "8a5f037e-...", "..."]
```

## Endpoint

| Method | URI           | Auth                            |
| ------ | ------------- | ------------------------------- |
| POST   | `/api/stores` | `Authorization` header required |

### Request body

```json
{
  "name": "Store Name",
  "brandId": "<uuid>",
  "description": "A short description of the store",
  "imageUrl": "https://example.com/image.jpg",
  "timezone": 1.0,
  "operatingHoursMap": {
    "MON": { "begin": { "hour": 9, "minute": 0 }, "end": { "hour": 22, "minute": 0 } }
  },
  "geoCoordinates": { "longitude": -9.1393, "latitude": 38.7169 }
}
```

### Field constraints

| Field              | Constraint                                                       |
| ------------------ | ---------------------------------------------------------------- |
| `name`             | 3–63 chars, pattern `^[a-zA-Z0-9\- ]+$`                         |
| `brandId`          | Required UUID                                                    |
| `description`      | 8–255 chars, pattern `^[a-zA-Z0-9\-, ]+$`                       |
| `imageUrl`         | Valid `http/https` URL ending in `.jpg/.jpeg/.png/.gif`          |
| `timezone`         | `-12` to `+14`                                                   |
| `operatingHoursMap`| Keys: `MON–SUN`; `hour`: 0–23, `minute`: 0–59                   |
| `geoCoordinates`   | `longitude`: −180 to +180, `latitude`: −90 to +90               |

### Responses

| Status | Meaning                                       |
| ------ | --------------------------------------------- |
| 200    | Store created, returns `StoreSummary`         |
| 400    | Validation failure or malformed JSON          |
| 403    | Missing or invalid API key                    |
| 409    | Store with same `name` + `brandId` exists     |
| 500    | Unexpected server error                       |

## Setup

```bash
# Build the project
./gradlew build

# Or on Windows
gradlew.bat build
```

## Running

```bash
# Run with Gradle (listens on http://localhost:8082)
./gradlew bootRun

# Or on Windows
gradlew.bat bootRun
```

The server listens on **`http://localhost:8082`** (configured in `src/main/resources/application.properties`).

## Project structure

```
StoreService/
├── config/
│   ├── api-keys.json        # Valid API keys
│   └── db.json              # Persisted store data
├── src/main/java/.../
│   ├── controller/
│   │   ├── StoreController.java        # POST /api/stores
│   │   └── GlobalExceptionHandler.java # 400/500 error mapping
│   ├── model/               # CreateStoreInput, StoreSummary, GeoCoordinates, etc.
│   └── service/
│       └── StoreService.java           # Business logic + persistence
├── src/main/resources/
│   └── application.properties         # server.port=8082
├── postman_collection.json
└── build.gradle.kts
```
