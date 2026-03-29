# Cloud Computing

## lab02

It is a standalone Java microservice implemented with Maven and the built-in `com.sun.net.httpserver` HTTP server. A server starts on port 8080 accepting CRUD operations for `/tables` and `/waiters`.
- Data is stored in PostgreSQL via JDBC using a `DATABASE_URL` loaded from `.env`.
- The service auto-creates the `tables` and `waiters` schema on startup.
- Responses are JSON serialized with Jackson, and custom exceptions return consistent error codes.

## lab04

It is composed of four independent projects:

- `Frontend/` — React + Vite UI on port `5173`
- `Backend/` — Django API gateway on port `8079`
- `RouteService/` — Spring Boot route optimization on port `8081`
- `StoreService/` — Spring Boot store persistence on port `8082`

The gateway forwards requests to backend services:
- `GET /api/waiters/{id}` → `FluxoService` on `8080` (no auth)
- `POST /api/routes` → `RouteService` on `8081` (API key)
- `POST /api/stores` → `StoreService` on `8082` (API key)

Quick run order:
1. Start `lab02` FluxoService first.
2. Start `RouteService`.
3. Start `StoreService`.
4. Start the Django `Backend` gateway.
5. Start the React `Frontend`.

For details on each subproject, see the nested `lab04/README.md` and the individual subdirectories.
