# HostelCare — Hostel Complaint & Maintenance Management Portal

**Version:** 1.0.0  
**Architecture:** Three-tier REST application  
**Frontend:** HTML5, CSS3, JavaScript, Tailwind CSS (CDN), Chart.js  
**Backend:** Java 21, Spring Boot 3.3.5, Spring Security, JWT, Spring Data JPA  
**Database:** MySQL 8.x

HostelCare centralizes hostel maintenance requests. Students submit and track complaints, maintenance staff process assigned work, and administrators manage hostel data, priorities, assignments and reports.

## 1. Implemented functionality

### Student
- Student-only self-registration, login and role-aware session handling.
- Profile and room information.
- Complaint creation with category, title, description and up to three image attachments.
- Complaint history, details, assignment information and status timeline.
- Confirm a resolved issue as closed or request reopening.
- One feedback rating (1–5) and optional comment after resolution.

### Staff
- Login and active assigned-task list.
- Complaint details and authorized attachment access.
- Update assigned tasks to `IN_PROGRESS` or `RESOLVED` with work remarks.
- Completed-work view.

### Admin / Warden
- Dashboard statistics and all-complaint view.
- Search and filter complaints by status and priority.
- Set priority manually and assign/reassign active staff.
- Create staff accounts and view students/staff.
- Deactivate non-admin user accounts through the API.
- Manage hostels, rooms and complaint categories.
- Status/category/hostel/staff reports and CSV export.

### Supporting features
- BCrypt password hashing and signed, expiring JWT authentication.
- Backend role and record-ownership checks.
- Nine relational tables with foreign keys, unique constraints and checks.
- Complaint history and assignment/reassignment records.
- JPEG, PNG and WebP uploads (maximum 3 files, 5 MB each), unique stored filenames and signature checks.
- Optional SMTP email notifications. Mail delivery failure does not discard a saved complaint.
- Validation and centralized API error responses.

## 2. Project structure

```text
HostelCare/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/hostelcare/
│       │   ├── config/       # Security, JWT and bootstrap data
│       │   ├── controller/   # REST endpoints and response mapping
│       │   ├── dto/          # Validated request DTOs
│       │   ├── entity/       # Nine JPA entities
│       │   ├── enums/        # Role, status and priority
│       │   ├── exception/    # API exception handling
│       │   ├── repository/   # Spring Data repositories
│       │   └── service/      # Authentication, complaints and email
│       └── resources/application.properties
├── frontend/
│   ├── index.html
│   ├── css/style.css
│   └── js/{api.js,app.js}
├── database/hostelcare_schema.sql
├── docs/{ARCHITECTURE.md,API.md,TESTING.md}
├── postman/HostelCare.postman_collection.json
├── .env.example
└── README.md
```

The frontend is a responsive, page-oriented vanilla JavaScript application with role-specific navigation. It uses `fetch()` against the Spring Boot REST API; it does not require React or a frontend build step. Tailwind CSS and Chart.js are loaded from their CDNs, so an internet connection is required for those CDN assets.

## 3. Prerequisites

- JDK 21 (verify with `java -version`).
- Apache Maven 3.9+ (verify with `mvn -version`).
- MySQL Server 8.x and a MySQL client / Workbench.
- Python 3 (or another static HTTP server) to serve the frontend.
- A modern browser.

## 4. Database setup

1. Start MySQL Server.
2. Open a MySQL client and execute `database/hostelcare_schema.sql`.
3. The script creates `hostelcare_db`, the nine tables, initial complaint categories, a `Main Hostel`, and sample rooms 101–103 and 201–203.
4. Create or identify a MySQL user with privileges on `hostelcare_db`.

Example SQL if you need a dedicated local account (choose your own password):

```sql
CREATE USER 'hostelcare_app'@'localhost' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
GRANT ALL PRIVILEGES ON hostelcare_db.* TO 'hostelcare_app'@'localhost';
FLUSH PRIVILEGES;
```

The backend uses `ddl-auto=validate`; it validates the supplied schema rather than silently modifying it. If you are using an existing database, back it up before applying schema changes.

## 5. Configure environment variables

Copy `.env.example` to `.env` in the project root and update the database credentials, a strong JWT secret, and an administrator email/password. Do not commit `.env`.

Spring Boot does not automatically load a generic `.env` file. Export its values into the process environment before starting the backend, or configure the same values in your IDE run configuration.

### Windows PowerShell

```powershell
Get-Content .env | Where-Object { $_ -match '^\s*[^#].+=' } | ForEach-Object {
  $parts = $_ -split '=', 2
  [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), 'Process')
}
```

### macOS / Linux

```bash
set -a
source .env
set +a
```

The `ADMIN_EMAIL` and `ADMIN_PASSWORD` variables are optional. If supplied, the backend creates that administrator on first startup only when the email does not already exist. There is no public admin or staff registration endpoint. Change the initial password after first login and use a unique, strong secret.

## 6. Start the backend

From the project root, create `.env` and run the included script (it loads `.env` automatically):

```powershell
.\start-backend.ps1
```

Or use the manual build path after loading environment variables as above:

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

Or run the packaged JAR after building:

```bash
java -jar target/hostelcare-backend-1.0.0.jar
```

Expected backend address: `http://localhost:8080`.

Check that MySQL is running and that `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are correct if startup fails. The application validates all nine tables during startup.

## 7. Start the frontend

Open a second terminal and run:

```powershell
.\start-frontend.ps1
```

Or run `python -m http.server 5500` from the `frontend` directory.

Visit **http://localhost:5500**. The default API base is `http://localhost:8080/api`.

If your backend uses another address, set `window.HOSTELCARE_API` before `js/api.js` loads in `frontend/index.html`, for example:

```html
<script>window.HOSTELCARE_API = 'http://localhost:8080/api';</script>
<script src="js/api.js"></script>
```

## 8. First-time demonstration flow

1. Sign in using the administrator email/password configured through `ADMIN_EMAIL` and `ADMIN_PASSWORD`.
2. Open **Hostels & rooms** and verify or add hostel rooms. Update the sample hostel address if needed.
3. Open **Users & staff** and create at least one staff account.
4. Sign out and create a student account using one of the available rooms.
5. Submit a complaint and optionally attach one or more supported images.
6. Sign in as admin, set the complaint priority and assign a staff member.
7. Sign in as the assigned staff member, open the task, move it to `IN_PROGRESS`, then `RESOLVED`, and add remarks.
8. Sign in as the student, inspect the history, submit feedback, and close the complaint or request reopening.
9. Sign in as admin and review Reports & analytics; export the CSV report.

## 9. API documentation

See [`docs/API.md`](docs/API.md) for endpoint details, request examples and role requirements. A Postman collection is included at `postman/HostelCare.postman_collection.json`. Set `baseUrl` and `token` variables in Postman. Authenticate first and copy the JWT from the login response into the token variable.

## 10. Security and operational notes

- Passwords are stored as BCrypt hashes. JWT signing secret and SMTP credentials are environment-driven.
- JWT is held in `sessionStorage` by this demonstration frontend, so it is cleared when the browser session ends. For a public production deployment, prefer secure, HttpOnly, SameSite cookies and HTTPS.
- Configure `FRONTEND_ORIGIN` to the exact frontend origin. Do not use broad wildcard CORS in production.
- Uploads are stored outside the public frontend tree in `UPLOAD_DIR`; download requests require complaint access authorization.
- Restrict upload directory permissions, back up uploaded files, and consider malware scanning and image re-encoding before public deployment.
- Email is optional. If SMTP is not configured, database workflows continue and no email is sent.
- Configure HTTPS, database least-privilege credentials, log retention, backups, monitoring and secret rotation before production use.
- The included bootstrap rooms and administrator are for local demonstration. Replace sample hostel data with actual hostel records.

## 11. Testing

Read [`docs/TESTING.md`](docs/TESTING.md) for a role-based manual test checklist and commands. Backend unit/integration tests should be added as the deployment environment and test database are finalized.

## 12. Known limitations / deployment checks

- This package contains source code and SQL, not a prebuilt JAR; Maven must resolve dependencies and build it on the target machine.
- The execution environment used to assemble this ZIP had JDK 21 but did not have Maven installed. Therefore a Maven compilation, Spring context startup, MySQL integration test, Postman run and browser end-to-end run could not be truthfully reported as passed here. Perform the clean build and test steps in `docs/TESTING.md` before demonstration.
- Tailwind and Chart.js are CDN dependencies; bundle them locally for an offline deployment.
- Report endpoints are intended for a small academic/demo deployment. Add pagination and optimized aggregate queries before handling large production datasets.
- The reports are optimized for small demonstration datasets; review the SQL aggregation approach and query plans before production-scale use.

## License

Academic project starter. Add the license required by your institution or deployment owner before redistribution.
