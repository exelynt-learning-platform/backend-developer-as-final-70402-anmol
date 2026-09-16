# Resource Booking API

A secure RESTful booking system built with Spring Boot 3, Java 17+, Spring Security, JWT, JPA/Hibernate, PostgreSQL, and OpenAPI.

## Requirements

- Java 17 or newer
- Maven 3.9+
- PostgreSQL 14+ (or another PostgreSQL-compatible database)

## Run locally

Create a database and set environment variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/booking"
$env:DB_USERNAME="booking"
$env:DB_PASSWORD="booking"
$env:JWT_SECRET="replace-with-a-random-secret-at-least-32-characters"
$env:SEED_DATA_ENABLED="true"
mvn spring-boot:run
```

`DDL_AUTO` defaults to `update`. Use `DDL_AUTO=validate` when schema migrations are managed externally. Demo seed data is disabled by default; set `SEED_DATA_ENABLED=true` only for local development. Tests use an in-memory H2 database.

Seed accounts are created on an empty database:

- `admin` / `Admin@123` with `ADMIN`
- `user` / `User@123` with `USER`

Change these credentials before using a shared environment.

## Authentication

```http
POST /auth/login
Content-Type: application/json

{"username":"user","password":"User@123"}
```

Send the returned token on protected requests:

```http
Authorization: Bearer <token>
```

Swagger UI is available at `/swagger-ui.html`; the OpenAPI document is `/v3/api-docs`.

## API

| Method | Endpoint                        | Access                        |
| ------ | ------------------------------- | ----------------------------- |
| GET    | `/resources`, `/resources/{id}` | ADMIN, USER                   |
| POST   | `/resources`                    | ADMIN                         |
| PUT    | `/resources/{id}`               | ADMIN                         |
| DELETE | `/resources/{id}`               | ADMIN                         |
| GET    | `/reservations`                 | ADMIN sees all; USER sees own |
| POST   | `/reservations`                 | ADMIN, USER                   |
| PATCH  | `/reservations/{id}/cancel`     | Owning USER                   |
| PUT    | `/reservations/{id}`            | ADMIN                         |
| DELETE | `/reservations/{id}`            | ADMIN                         |

Reservation creation ignores any owner field in the request. The authenticated username from the JWT is stored as the reservation owner. Request fields are `resourceId`, `price`, `startTime`, `endTime`, and optional `status` (`PENDING`, `CONFIRMED`, or `CANCELLED`). Times are ISO-8601 instants and `endTime` must be after `startTime`.

Reservations reject unavailable resources and overlapping active reservations. Intervals use half-open semantics: a booking ending exactly when another starts is allowed. Resource locking prevents concurrent requests from passing the overlap check at the same time. Users create pending reservations and can cancel only their own reservations; administrators control reservation updates and statuses.

Reservation search supports:

```text
GET /reservations?status=CONFIRMED&minPrice=10&maxPrice=500&page=0&size=20&sort=price,desc
```

Allowed sort fields are `id`, `price`, `startTime`, `endTime`, and `status`. Size is limited to 100.

## Verification

```powershell
mvn test
mvn package
```

Authentication is stateless, passwords are BCrypt-hashed, and invalid or unauthorized requests return standard 401/403 responses. Validation and domain failures return a JSON error body with timestamp, status, error, and message.
