# Animal Farm Management System

Full-stack starter for managing:
- Cattles
- Goats
- Rams
- Pigs

Tech stack:
- Backend: Java 21 + Spring Boot
- Database: MySQL
- Frontend: React + Vite

## Project Structure

- `backend` Spring Boot API
- `frontend` React client

## Backend Setup

1. Create MySQL user/database (or use defaults in `backend/src/main/resources/application.yml`).
2. Update DB credentials if needed.
3. Run:

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8080/api`

## Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Docker Deployment

Run the full stack (MySQL + backend + frontend):

```bash
docker compose up --build
```

Services:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- MySQL: `localhost:3307` (inside compose network it is `db:3306`)

Stop services:

```bash
docker compose down
```

Stop and remove DB volume:

```bash
docker compose down -v
```

## Implemented Features

- Register animal owner (admin only)
- Register animal (admin only)
- Transfer animal(s) from one owner to another if:
  - acting user is owner of the animal(s), or
  - acting user is admin
- Owner creates transfer request to admin (with email message content)
- Admin approves/rejects transfer requests
- Admin sells animal to market
- PDF reports:
  - Owner vs Animal
  - Parent vs Animal
  - Owner animal report

## Main Endpoints

- `POST /api/owners` (header `X-Actor-Role: ADMIN`)
- `POST /api/animals`
- `GET /api/animals`
- `POST /api/animals/transfer`
- `POST /api/animals/{animalId}/sell`
- `POST /api/transfer-requests`
- `GET /api/transfer-requests`
- `POST /api/transfer-requests/{id}/approve`
- `POST /api/transfer-requests/{id}/reject`
- `GET /api/reports/owner-vs-animal?ownerId=...`
- `GET /api/reports/parent-vs-animal?parentId=...`
- `GET /api/reports/owner/{ownerId}`

## Notes

- This version uses request roles (`OWNER` / `ADMIN`) for authorization decisions.
- For production, add real authentication and authorization (JWT/session, users, password policy, audit logs).
