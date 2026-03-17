# API Gateway

A Django-based API gateway that authenticates incoming requests and forwards them to three internal services.

## Architecture

```
Client
  │  Authorization: <gateway-api-key>
  ▼
API Gateway  :8079  (http)
  ├── GET  /api/waiters/{id}  ──▶  FluxoService   :8080  (no auth)
  ├── POST /api/routes        ──▶  RouteService   :8081  (ROUTE_SERVICE_API_KEY)
  └── POST /api/stores        ──▶  StoreService   :8082  (STORE_SERVICE_API_KEY)
```

## Authentication

Every request must include an `Authorization` header containing a valid API key.
Valid keys are stored in `config/api_keys.json`. Requests with a missing or unrecognised key receive **403 Forbidden**.

Once validated, the gateway strips the client key and forwards the request with the appropriate service-level key (loaded from `.env`). FluxoService does not require a key, so no `Authorization` header is forwarded to it.

## Configuration

### `.env`

```dotenv
FLUXO_SERVICE_URL="http://localhost:8080"
ROUTE_SERVICE_URL="http://localhost:8081"
STORE_SERVICE_URL="http://localhost:8082"

ROUTE_SERVICE_API_KEY="<key-for-route-service>"
STORE_SERVICE_API_KEY="<key-for-store-service>"
```

### `config/api_keys.json`

A JSON array of accepted gateway API keys:

```json
["8a5f037e-...", "9b4b09e9-...", "..."]
```

## Endpoints

| Method | Gateway URI               | Upstream target       |
| ------ | ------------------------- | --------------------- |
| GET    | `/api/waiters/{waiterId}` | `/waiters/{waiterId}` |
| POST   | `/api/routes`             | `/api/routes`         |
| POST   | `/api/stores`             | `/api/stores`         |

### Request bodies

**POST /api/routes**

```json
{
  "storeId": "<uuid>",
  "standIdList": ["<uuid>", "<uuid>"]
}
```

**POST /api/stores**

```json
{
  "name": "Store Name",
  "brandId": "<uuid>",
  "description": "A short description of the store",
  "imageUrl": "http://example.com/image.jpg",
  "timezone": 1.0,
  "operatingHoursMap": {
    "MON": {
      "begin": { "hour": 9, "minute": 0 },
      "end": { "hour": 22, "minute": 0 }
    }
  },
  "geoCoordinates": { "longitude": -9.1393, "latitude": 38.7169 }
}
```

## Setup

```bash
# 1. Create a virtual environment
python -m venv .venv

# 2. Install all dependencies
.venv\Scripts\python.exe -m pip install -r requirements.txt

# 3. Copy and fill in the service API keys
copy .env.example .env
```

## Running

```bash
# From the project root
bin\start.bat

# Or directly
.venv\Scripts\python.exe manage.py runsslserver 8079
```

The server listens on **`http://localhost:8079`** with a self-signed certificate.

## Logging

Every request emits three structured log lines to stdout:

```
[2026-03-16 22:42:01] INFO     proxy  [INBOUND]  POST /api/routes  body='...'
[2026-03-16 22:42:01] INFO     proxy  [FORWARD]  POST http://localhost:8081/api/routes  body='...'
[2026-03-16 22:42:01] INFO     proxy  [RESPONSE] POST http://localhost:8081/api/routes → 200  body='...'
```

Rejected requests emit a `WARNING`:

```
[2026-03-16 22:42:05] WARNING  proxy  [AUTH] 403 Forbidden  POST /api/routes
```

## Project structure

```
Backend/
├── bin/
│   └── start.bat          # Start script (http, port 8079)
├── config/
│   └── api_keys.json      # Valid gateway API keys
├── gateway/               # Django project settings & URL root
│   ├── settings.py
│   └── urls.py
├── proxy/                 # Gateway app
│   ├── views.py           # Auth validation + request forwarding
│   └── urls.py            # Route definitions
├── .env                   # Service API keys (git-ignored)
├── .env.example
├── manage.py
├── requirements.txt       # Pinned dependencies
└── postman_collection.json
```
