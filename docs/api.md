# API Contract (Implemented Slice)

Base URL: `http://localhost:8080/api/v1`. JSON errors from Spring include an HTTP status and message. JWTs expire after `JWT_EXPIRATION` milliseconds. All protected calls send `Authorization: Bearer <token>`.

Error responses use `timestamp`, `status`, `code`, `message`, `path`, and `fieldErrors`. Invalid request fields return `400`, duplicate account identifiers return `409`, missing authentication returns `401`, and out-of-scope reads return `403`.

## Authentication

### `POST /auth/register`

Creates a student account. Public registration can create only the `STUDENT` role.

```json
{
  "email": "student@example.edu",
  "password": "at-least-12-characters",
  "fullName": "Example Student",
  "studentNumber": "CS2026001",
  "program": "Computer Science",
  "semester": 4
}
```

### `POST /auth/login`

```json
{ "email": "student@example.edu", "password": "at-least-12-characters" }
```

### Auth response

```json
{
  "token": "<signed-token>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "email": "student@example.edu",
  "role": "STUDENT",
  "studentId": 1
}
```

### `GET /auth/me`

Returns the authenticated email, role, and linked student ID when applicable.

## Digital Twin

### `GET /students/{studentId}/digital-twin`

Only the linked student or an `ADMIN` can read the profile in the current slice. Teacher assignment authorization is not yet implemented.

Response includes:

- `studentId`, `studentName`, `overallScore`, and `classification`.
- `metrics`: academic, attendance, skills, learning progress, projects, activities, and certifications.
- `missingData`: dimensions without source records.
- `learningGaps`: subject, measured score, and `Performance-based learning gap indicator` label.
- `riskIndicators` and `recommendations`, each with category, priority, and reason/action details.

No student data is seeded. A newly registered profile therefore has `overallScore: null`, `classification: "INSUFFICIENT_DATA"`, and an empty-data explanation until permitted academic records are added through future module APIs.
