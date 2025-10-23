# Brokerage Core

A Spring Boot backend API for a brokerage firm. Employees can create, list, cancel, and match stock orders for customers. Assets and orders are stored in an H2 in-memory database. JWT-based authentication is provided with ADMIN and CUSTOMER roles.

## Build and Run

- Prerequisites: Java 21, Maven wrapper (included)
  - Start the app:
    - `mvn spring-boot:run`
- Profiles:
  - `dev` (default for local run via `-Dspring-boot.run.profiles=dev`) – H2 in‑memory, H2 console enabled at `/h2-console`.
  - `test` - H2 in‑memory, console disabled.
  - `preprod` – external DB via env, ddl-auto validate.
  - `prod` – external DB via env, ddl-auto validate.
- Default port: `8080`

H2 datasource: `jdbc:h2:mem:stock_db` (in-memory)

## Quick Start (Dev/Test)

1) Create an admin (dev/local/test profile only)
- Endpoint: `POST /api/setup/admin`
- Body:
```
{ "username": "admin", "password": "admin123" }
```
- Response contains created admin id/credentials (for test convenience).

2) Login and get access token
- Endpoint: `POST /api/auth/login`
- Body:
```
{ "username": "admin", "password": "admin123" }
```
- Use `data.accessToken` as a Bearer token for subsequent requests.

3) Create initial assets for a customer (example)
- First create a customer as ADMIN:
  - Endpoint: `POST /api/customers`
  - Body:
```
{ "username": "cust1", "password": "custpass", "role": "CUSTOMER" }
```
- Seed TRY via dev-only endpoint (easier with Postman):
  - Endpoint: `POST /api/setup/assets/upsert` (requires ADMIN token)
  - Body:
```
{ "customerId": "<uuid>", "assetName": "TRY", "size": 100000, "usableSize": 100000 }
```

## Endpoints

All business endpoints require `ROLE_ADMIN` by default. JWT is required as `Authorization: Bearer <accessToken>`.

- Orders
  - Create: `POST /api/orders`
```
{
  "customerId": "<uuid>",
  "assetName": "AAPL",
  "orderSide": "BUY" | "SELL",
  "size": 10.0,
  "price": 100.0
}
```
  - List by date range: `POST /api/orders/list`
```
{ "customerId": "<uuid>", "start": "2025-01-01T00:00:00", "end": "2025-12-31T23:59:59" }
```
  - Cancel: `DELETE /api/orders/{orderId}` (only PENDING)
  - List pending: `GET /api/orders/pending`
  - Match order: `POST /api/orders/{orderId}/match` (Bonus 2)

- Assets
  - List by customer: `GET /api/assets/{customerId}`

- Auth
  - Register: `POST /api/auth/register` (creates CUSTOMER)
  - Login: `POST /api/auth/login`
  - Refresh: `POST /api/auth/refresh?refreshToken=...`
  - Logout: `POST /api/auth/logout?refreshToken=...`

## Business Rules (Highlights)

- Orders have status: `PENDING`, `MATCHED`, `CANCELED`.
- TRY is also an asset (no separate table), asset name `TRY`.
- Create order:
  - BUY: check customer TRY `usableSize` >= `size * price`; reserve by reducing TRY `usableSize`.
  - SELL: check asset `usableSize` >= `size`; reserve by reducing asset `usableSize`.
- Cancel order (PENDING only): reverse the reservation.
- Match order (admin):
  - BUY: reduce TRY `size` by `size * price`; increase bought asset `size` and `usableSize`.
  - SELL: reduce sold asset `size`; increase TRY `size` and `usableSize` by `size * price`.
  - Orders on `assetName = TRY` are rejected (TRY is quote currency, not tradable).

## Tests

Run all tests:
-  ` mvn test`

The suite includes focused service tests for order creation, cancellation, matching, and listing logic using H2.

## Notes

- Profiles `dev/test` enable the admin setup endpoint.
- You can explore the schema in H2 console at `/h2-console` with JDBC URL `jdbc:h2:mem:stock_db`.
