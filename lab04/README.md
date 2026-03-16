# Requirements

[Official requirements](https://docs.google.com/document/d/1a8ZhuanwOWL30ImqMnLcvPs9DPZuf7Y2nKwD0uOa-oU/edit?tab=t.0)

- [x] frontend - React
- [x] backend - Django - calls the 3 web services
- [x] 3 web services
- [x] authentication
- [x] error handling
- [x] json communication

A cloud computing lab composed of four independent projects: a React frontend, a Django API gateway, and two Spring Boot microservices.

## Architecture

```
Browser (Frontend :5173)
  │  VITE_GATEWAY_URL / VITE_GATEWAY_KEY
  ▼
API Gateway (Backend :8079  HTTPS)
  ├── GET  /api/waiters/{id}  ──▶  FluxoService   :8080  (no auth)
  ├── POST /api/routes        ──▶  RouteService   :8081  (API key)
  └── POST /api/stores        ──▶  StoreService   :8082  (API key)
```

## Projects

| Directory       | Description                                                       | Port |
| --------------- | ----------------------------------------------------------------- | ---- |
| `Frontend/`     | React + Vite SPA — triggers gateway endpoints and shows responses | 5173 |
| `Backend/`      | Django API gateway — authenticates requests and forwards them     | 8079 |
| `RouteService/` | Spring Boot service — creates optimised waiter routes for a store | 8081 |
| `StoreService/` | Spring Boot service — creates and persists store entries          | 8082 |

> FluxoService (port 8080) is an external dependency not included in this repository.

## Quick start

Start each project in a separate terminal following the order below so dependencies are available when the gateway makes its first forward.

### 1. RouteService

```bash
cd RouteService
gradlew.bat bootRun
```

### 2. StoreService

```bash
cd StoreService
gradlew.bat bootRun
```

### 3. Backend (API Gateway)

```bash
cd Backend

# Windows
copy .env.example .env
# Linux/Mac
cp .env.example .env
# then fill in API keys and service URLs

# Windows
bin\start.bat
# Linux/Mac
./bin/start.sh
```

### 4. Frontend

```bash
cd Frontend

# Windows
copy .env.example .env
# Linux/Mac
cp .env.example .env
# then fill in VITE_GATEWAY_URL and VITE_GATEWAY_KEY

npm install
npm run dev
```

Open [https://localhost:8079](https://localhost:8079) to call the gateway directly, or [http://localhost:5173](http://localhost:5173) for the browser UI.

## Authentication

- The **Frontend → Gateway** leg uses a key from `Backend/config/api_keys.json` (set as `VITE_GATEWAY_KEY`).
- The **Gateway → RouteService** leg uses `ROUTE_SERVICE_API_KEY` from `Backend/.env`.
- The **Gateway → StoreService** leg uses `STORE_SERVICE_API_KEY` from `Backend/.env`.
- FluxoService requires no key.

## References

- [Official requirements](https://docs.google.com/document/d/1a8ZhuanwOWL30ImqMnLcvPs9DPZuf7Y2nKwD0uOa-oU/edit?tab=t.0)
- Per-project details: see each subdirectory's `README.md`
