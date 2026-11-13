# HostelCare REST API

Base URL: `http://localhost:8080`  
All protected requests use `Authorization: Bearer <JWT>`. JSON requests use `Content-Type: application/json` unless noted.

## Authentication and public catalog

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Student registration |
| POST | `/api/auth/login` | Public | Login; returns JWT and user data |
| GET | `/api/auth/me` | Authenticated | Current user profile |
| GET | `/api/public/catalog` | Public | Hostels, rooms and complaint categories for registration |

Student registration body:

```json
{"name":"A Student","email":"student@example.edu","password":"ChangeMe123!","phone":"9876543210","roomId":1}
```

Login body:

```json
{"email":"student@example.edu","password":"ChangeMe123!"}
```

## Student

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/student/profile` | STUDENT / ADMIN | Student profile |
| GET | `/api/student/complaints` | STUDENT / ADMIN | Own complaint list |
| POST | `/api/complaints` | Authenticated student | Create complaint (JSON or multipart) |
| GET | `/api/complaints/{id}` | Owner / assigned staff / ADMIN | Complaint details and history |
| PUT | `/api/complaints/{id}/close` | Complaint owner | Confirm resolution |
| PUT | `/api/complaints/{id}/reopen` | Complaint owner | Request reopening |
| POST | `/api/complaints/{id}/feedback` | Complaint owner | Submit one feedback rating |
| GET | `/api/complaints/{id}/attachments/{attachmentId}` | Owner / assigned staff / ADMIN | Download/view image |

Multipart complaint: send a JSON part named `data` with `title`, `description`, `categoryId`, and repeat image parts named `images`. Maximum 3 images, 5 MB each; JPEG, PNG and WebP only. JSON-only creation is also supported:

```json
{"title":"Leaking tap","description":"Water is leaking continuously below the washbasin.","categoryId":2}
```

Feedback body:

```json
{"rating":5,"comments":"Issue was fixed promptly."}
```

## Admin / warden

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/dashboard` | ADMIN | Summary counts |
| GET | `/api/admin/complaints` | ADMIN | All complaints; optional `status`, `priority`, `category`, `hostel`, `from`, `to`, `q` filters |
| PUT | `/api/admin/complaints/{id}/priority` | ADMIN | Set `LOW`, `MEDIUM`, `HIGH` |
| POST | `/api/admin/complaints/{id}/assign` | ADMIN | Assign/reassign staff |
| GET | `/api/admin/students` | ADMIN | Active students |
| GET | `/api/admin/staff` | ADMIN | Active staff |
| POST | `/api/admin/staff` | ADMIN | Create staff account |
| PUT | `/api/admin/users/{id}/deactivate` | ADMIN | Deactivate non-admin account |
| GET / POST | `/api/admin/hostels` | ADMIN | List / create hostel |
| GET / POST | `/api/admin/rooms` | ADMIN | List / create room |
| GET / POST | `/api/admin/categories` | ADMIN | List / create category |

Priority body: `{"priority":"HIGH"}`. Assignment body: `{"staffId":2}`. Staff creation body: `{"name":"Maintenance Staff","email":"staff@example.edu","password":"ChangeMe123!","phone":"9876543210"}`.

Hostel body: `{"hostelName":"Main Hostel","blockName":"Block A","address":"Campus road"}`. Room body: `{"hostelId":1,"roomNumber":"204","floor":2,"capacity":3}`. Category body: `{"categoryName":"Lift","description":"Lift maintenance"}`.

## Staff

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/staff/assignments` | STAFF / ADMIN | Active assigned complaints |
| GET | `/api/staff/complaints/{id}` | Assigned staff / ADMIN | Complaint details |
| PUT | `/api/staff/complaints/{id}/status` | Assigned staff / ADMIN | Set `IN_PROGRESS` or `RESOLVED` |
| GET | `/api/staff/history` | STAFF / ADMIN | Resolved/closed active assignments |

Status body: `{"status":"IN_PROGRESS","remarks":"Inspected the pipe; replacement part required."}`. Staff cannot update a complaint that is not actively assigned to them.

## Reports

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/admin/reports/summary` | ADMIN | Status totals and feedback count |
| GET | `/api/admin/reports/categories` | ADMIN | Category counts |
| GET | `/api/admin/reports/hostels` | ADMIN | Hostel counts |
| GET | `/api/admin/reports/priority` | ADMIN | Priority counts |
| GET | `/api/admin/reports/staff` | ADMIN | Active assignment workload |
| GET | `/api/admin/reports/export` | ADMIN | CSV export |

## Response conventions

- `200` successful read/update.
- `201` created resources (some create methods return `200` in this initial API implementation).
- `400` validation or business-rule failure.
- `401` missing/invalid/expired JWT.
- `403` authenticated role lacks permission.
- `404` resource missing or inaccessible to caller.
- `409` duplicate or database constraint conflict.
- `500` unexpected server failure.

Errors are JSON objects such as `{"error":"Invalid email or password."}`. Do not expose stack traces to clients.
