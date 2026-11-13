# HostelCare Testing Checklist

## Build and startup

- [ ] JDK 21 is active (`java -version`).
- [ ] Maven is available (`mvn -version`).
- [ ] MySQL 8.x is running and `database/hostelcare_schema.sql` executed.
- [ ] Environment variables are loaded and `ADMIN_EMAIL` / `ADMIN_PASSWORD` configured.
- [ ] `mvn clean package` succeeds.
- [ ] Spring Boot starts on port 8080 and Hibernate schema validation succeeds.
- [ ] Frontend is served on port 5500 and can reach the API.

## Authentication and authorization

- [ ] Register a student with a valid room and password of at least 8 characters.
- [ ] Duplicate email registration is rejected.
- [ ] Invalid password login is rejected.
- [ ] A student cannot access `/api/admin/dashboard` (403).
- [ ] An unauthenticated request to `/api/auth/me` is rejected (401).
- [ ] A student cannot retrieve another student's complaint (404).
- [ ] A staff member cannot retrieve a complaint not assigned to them (404).
- [ ] Expired or malformed JWT is rejected.

## Complaint lifecycle

- [ ] Student can submit a valid complaint.
- [ ] Empty title/description and invalid category are rejected.
- [ ] Initial `SUBMITTED` history entry is created.
- [ ] More than 3 attachments are rejected.
- [ ] Image above 5 MB is rejected.
- [ ] Unsupported type and invalid image signature are rejected.
- [ ] Admin can set priority and assign an active staff account.
- [ ] Reassignment deactivates the old assignment and retains its history row.
- [ ] Staff can set assigned complaint to `IN_PROGRESS`, then `RESOLVED`.
- [ ] Staff cannot update an unassigned complaint or use an unsupported status.
- [ ] Each transition creates a history row.
- [ ] Student can close a resolved complaint or reopen it if the issue persists.
- [ ] Feedback is rejected before resolution and duplicate feedback is rejected.
- [ ] Feedback rating outside 1–5 is rejected.

## Administration and reports

- [ ] Admin can create staff, hostel, room and category records.
- [ ] Duplicate hostel-room pair is rejected by database constraint.
- [ ] Student and staff lists show active accounts only.
- [ ] Dashboard counts match seeded/test data.
- [ ] Category and hostel report totals match manual SQL counts.
- [ ] CSV export opens correctly in a spreadsheet application.

## Useful manual SQL checks

```sql
USE hostelcare_db;
SELECT status, COUNT(*) FROM complaints GROUP BY status;
SELECT category_id, COUNT(*) FROM complaints GROUP BY category_id;
SELECT complaint_id, old_status, new_status, changed_at
FROM complaint_history ORDER BY complaint_id, changed_at;
SELECT complaint_id, staff_id, is_active FROM assignments ORDER BY complaint_id, assigned_at;
SELECT COUNT(*) FROM feedback;
```

## Postman

Import `postman/HostelCare.postman_collection.json`. Set collection variables:

- `baseUrl`: `http://localhost:8080`
- `token`: JWT returned from login.
- `roomId`, `categoryId`, `complaintId`, `staffId`: IDs from your local seeded/test data.

Run the authentication request first and set the token variable before protected requests.

## Browser smoke test

- [ ] Login and registration views render on desktop and mobile widths.
- [ ] Student can create a complaint and see it in My complaints.
- [ ] Admin can open complaint, set priority and assign staff.
- [ ] Staff can resolve task with remarks.
- [ ] Student can submit feedback and close/reopen appropriately.
- [ ] Admin reports render charts and CSV downloads.

**Verification note:** The source package was assembled in an environment with JDK 21 but without Maven or a configured MySQL server. Consequently the build, Spring context, SQL integration and browser tests have not been executed here. Run the checklist in a local environment before presenting the application as verified.
