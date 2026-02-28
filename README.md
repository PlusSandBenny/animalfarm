# Animal Farm Management System

Full-stack starter for managing:
- Cattles
- Goats
- Rams
- Pigs

Tech stack:
- Backend: Java 17 + Spring Boot
- Database: MySQL
- Frontend: React + Vite

## Project Structure

- `backend` Spring Boot API
- `frontend` React client

## How To Run

### Option 1: Run with Docker (recommended)

1. Install Docker Desktop.
2. From the project root, run:

```bash
docker compose up --build
```

3. Open:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`

Stop:

```bash
docker compose down
```

Reset everything including database volume:

```bash
docker compose down -v
```

### Option 2: Run locally (without Docker)

Prerequisites:
- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+

1. Start MySQL and create/update credentials in `backend/src/main/resources/application.yml`.
2. Start backend:

```bash
cd backend
mvn spring-boot:run
```

3. In a new terminal, start frontend:

```bash
cd frontend
npm install
npm run dev
```

4. Open:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`

## Authentication And Roles

The app now has a login page and role-based dashboards:
- `ADMIN` dashboard: owner registration, animal registration, all transfers, market sales, transfer approvals/rejections, admin reports.
- `OWNER` dashboard: own animals, own transfers, create admin transfer request, owner report.

Authentication model:
- Users are stored in MySQL (`app_users`) with hashed passwords (BCrypt).
- Access uses JWT access token + refresh token.
- Admin is bootstrapped from config:
  - username: `${APP_AUTH_ADMIN_USERNAME:admin}`
  - password: `${APP_AUTH_ADMIN_PASSWORD:admin123}`
- Admin creates owner profile and owner login credentials together.
- Admin can set credentials later for owners that do not yet have usernames.
- If admin sets/resets an owner password, owner must change password at next login.

### Option 3: Run with Vagrant (auto-config + auto-deploy)

Prerequisites:
- VirtualBox
- Vagrant

From the project root:

```bash
vagrant up
```

What happens automatically:
- Ubuntu VM is created
- Docker and Docker Compose are installed in the VM
- Application is cloned from `https://github.com/PlusSandBenny/animalfarm.git` into `/opt/animalfarm`
- Branch defaults to `dev` (configurable with `APP_BRANCH`)
- `docker compose up -d --build` is executed from the cloned repo

Access from host machine:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- MySQL: `localhost:3307`

Useful commands:

```bash
vagrant ssh
vagrant halt
vagrant reload --provision
vagrant destroy -f
```

Use a different branch when needed:

PowerShell:

```powershell
$env:APP_BRANCH="main"
vagrant reload --provision
```

Bash:

```bash
APP_BRANCH=main vagrant reload --provision
```

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

Services in compose:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- MySQL: `localhost:3307` (inside compose network it is `db:3306`)

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

- `POST /api/owners`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/owners`
- `GET /api/owners/search?ownerId=...`
- `GET /api/owners/search?firstName=...`
- `PUT /api/owners/{ownerId}`
- `GET /api/invoice-parameters`
- `PUT /api/invoice-parameters`
- `GET /api/invoices/monthly/owner/{ownerId}`
- `GET /api/invoices/monthly/owners`
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

- This version enforces role-based access from authenticated JWT claims (`OWNER` / `ADMIN`).
- Transfer and sell operations are written to `audit_logs`.

## Error Log Location

Backend error logs are written to:

- Container path: `/var/log/animalfarm/error.log`
- Project path on VM/host (via compose volume): `logs/backend/error.log`

To change the log file location, set environment variable:

```bash
APP_ERROR_LOG_FILE=/your/path/error.log
```

## UUID Migration (Required For Existing Databases)

This version uses:
- `owners.id` as internal integer PK, and `owners.owner_id` as UUID business key.
- `animals.id` as internal integer PK, and `animals.animal_id` as UUID business key.
- Owner/animal API operations now use UUID values.

If you already have data from older versions, run:

```bash
mysql -h 127.0.0.1 -P 3307 -u root -proot animalfarm < backend/db/migrations/2026-02-28-uuid-identifier-migration.sql
```

For Vagrant VM:

```bash
vagrant ssh -c "cd /opt/animalfarm && docker compose exec -T db mysql -uroot -proot animalfarm < backend/db/migrations/2026-02-28-uuid-identifier-migration.sql"
```

Then restart the application containers:

```bash
docker compose up -d --build
```
