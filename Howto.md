# Animal Farm Application User Manual

## 1. Overview

This application manages:
- Cattles
- Goats
- Rams
- Pigs

It supports:
- Animal registration
- Owner registration
- Animal transfer
- Selling animals to market
- PDF report generation
- Role-based access (`ADMIN` and `OWNER`)

## 2. Accessing the Application

Open the frontend in your browser:
- `http://localhost:5173`

## 3. Login

You must log in first.

Demo accounts:
- `admin / admin123`
- `owner1 / owner123` (mapped owner id: `1`)
- `owner2 / owner123` (mapped owner id: `2`)

After login, the system opens the correct dashboard based on role.

## 4. Admin User Guide

Admin users can access all core management features.

### 4.1 Register Animal Owner
1. Go to **Admin Dashboard**.
2. Fill the **Register Owner** form:
- First name
- Last name
- Email
- Phone
- Address
3. Click **Create Owner**.

### 4.2 Register Animal
1. In **Register Animal** form, provide:
- Animal ID
- Color
- Date of birth
- Breed
- Type (`CATTLE`, `GOAT`, `RAM`, `PIG`)
- Image URL (optional)
- Parent animal DB id (optional)
- Owner ID
2. Click **Create Animal**.

### 4.3 Transfer Animal(s)
1. In **Transfer Animals** section:
- Enter destination owner ID.
- Enter animal DB IDs (comma-separated, e.g. `10,11,12`).
2. Click **Transfer**.

### 4.4 Approve/Reject Owner Transfer Requests
1. Open **Transfer Requests** table.
2. Click:
- **Approve** to complete transfer.
- **Reject** to deny request.

### 4.5 Sell Animal to Market
1. In **Animals** table, find the animal.
2. Click **Sell**.
3. Animal status becomes sold.

### 4.6 Generate Reports (PDF)
Admin can generate:
- **Owner vs Animal**
- **Owner Animal**
- **Parent vs Animal**

Steps:
1. Enter required ID(s) in **Admin Reports**.
2. Click the relevant report button.
3. A PDF file downloads automatically.

## 5. Owner User Guide

Owners only manage their own data and animals.

### 5.1 View Own Animals
1. Log in as owner.
2. **Owner Dashboard** shows only your animals in **Your Animals** table.

### 5.2 Transfer Own Animals Directly
1. In **Transfer Your Animals**:
- Enter destination owner ID.
- Enter your animal DB IDs (comma-separated).
2. Click **Transfer**.

Note: Transfer is denied if you try animals you do not own.

### 5.3 Send Transfer Request to Admin
1. In **Email Request to Admin**:
- Confirm from owner id (read-only).
- Enter destination owner ID.
- Enter animal DB IDs.
- Enter message.
2. Click **Send Request**.

### 5.4 Download Owner Report
1. In **Your Report**, click **Download Owner Animal Report**.
2. PDF downloads for your owner profile only.

## 6. Role Rules

### Admin
- Register owners
- Register animals
- Transfer any animal
- Approve/reject transfer requests
- Sell animals to market
- Access admin reports

### Owner
- View own profile/animals
- Transfer own animals
- Create transfer request to admin
- Download own owner report

## 7. Logout

Use **Logout** button in the top right of the dashboard.

## 8. Common Issues

### 8.1 “Missing bearer token” / Unauthorized
- Cause: Not logged in or session expired.
- Fix: Log in again.

### 8.2 Owner cannot access another owner’s data
- Expected behavior due to role restrictions.

### 8.3 Transfer denied
- Cause: Owner tried transferring animal not owned by them, or animal is sold.

### 8.4 No report downloaded
- Check entered ID values.
- Check browser popup/download permission.

## 9. API Access Notes

The frontend uses token-based authentication:
- Login endpoint: `POST /api/auth/login`
- Protected endpoints require `Authorization: Bearer <token>`

## 10. Recommended Next Improvements

- Replace demo users with database users
- Add password hashing
- Use JWT with expiration/refresh
- Add audit logs for transfer and sale actions
