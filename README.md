# Digital Twin of a Student

A role-aware academic and career development application that calculates a student Twin from source records. The current implementation includes student registration/login, JWT-authenticated APIs, a MySQL schema migration, and a responsive Twin dashboard. Academic/attendance/skill record management and teacher/admin workflows remain in progress; the dashboard never invents missing records.

## Architecture

```text
React + Vite → JSON REST API → Spring Boot services/security → JPA/Flyway → MySQL
```

The backend uses Spring Boot 3.5.16, Java 17+, Spring Security, JPA, Bean Validation, Flyway, and MySQL. The frontend uses React, Vite, React Router, Axios, and responsive CSS. API documentation is served at `/swagger-ui.html` when the backend is running.

## Current functionality

- Student account registration and sign-in with BCrypt password hashing and JWT.
- Student-owned Twin endpoint: `GET /api/v1/students/{studentId}/digital-twin`.
- Aggregates academic performance, attendance, skill score, learning progress, projects, activities, and certifications from database records.
- Configurable weighted overall score and classification thresholds.
- Performance-based learning gap indicators and explainable attendance/academic/skill recommendations.
- Missing metrics are excluded from score calculation and disclosed in `missingData`.
- Responsive dashboard with accessible loading, empty, and error states.
- CSS progress indicators visualize the aggregated Twin dimensions; historical trend charts are not implemented yet.

## Twin calculation

Configured weights are academic 35%, attendance 15%, skills 20%, learning progress 15%, project participation 10%, and activities 5%. The score is the weighted mean of available dimensions, normalized by the sum of their weights, clamped to 0–100. Missing dimensions are not scored as zero. Project and activity participation currently use a transparent demo heuristic of 20 points per recorded item, capped at 100; certifications are shown as an indicator but do not contribute to the score. Classification defaults: Excellent ≥90, Good ≥80, Average ≥65, Needs improvement ≥50, otherwise At risk. Attendance risk defaults below 75%; academic gap indicator defaults below 60%. Tune these properties in `backend/src/main/resources/application.yml` before institutional use.

## Database

Flyway migration `V1__initial_schema.sql` defines users, students/teachers, assignments, departments, terms, subjects/enrollment, attendance, assessments/results, skills, certificates, projects, activities, learning goals/progress, career goals/required skills, recommendations, remarks, and audit events. Hibernate validates the migration schema. Records use restrictive foreign keys rather than cascade deletion.

## Requirements

- Java 17 or newer (Spring Boot 3.5 requires Java 17+).
- Maven 3.6.3+.
- Node.js 20.19+ or 22.12+ (current Vite requirement).
- Docker Compose for the local MySQL service, or a compatible MySQL 8 database.

## Run locally

On Windows, from the project folder, run `./run-local.ps1`. It starts the backend with the temporary local H2 profile, starts Vite, checks both HTTP endpoints, and prints the dashboard URL. Run `./stop-local.ps1` to stop the two processes it started.

1. For a quick demo without configuring MySQL, start the backend with the `local` profile and a temporary `JWT_SECRET` of at least 32 bytes: `mvn spring-boot:run -Dspring-boot.run.profiles=local`. This profile uses a temporary in-memory H2 database; its data is discarded when the backend stops.
2. For MySQL persistence, copy `.env.example` to `.env`, replace the local database/JWT secrets, and ensure `JWT_SECRET` has at least 32 bytes.
3. Start MySQL: `docker compose --env-file .env up -d mysql`, or use a configured MySQL 8 server.
4. In `backend`, export the `.env` values into the shell environment, then run `mvn spring-boot:run` without the `local` profile.
5. In another terminal, from `frontend`, set `VITE_API_URL` if needed, run `npm install`, then `npm run dev`.
6. Open `http://localhost:5173`, create a student account, and sign in.

PowerShell example to load values from `.env` for the current session:

```powershell
Get-Content ..\.env | Where-Object { $_ -match '^[A-Z_]+=.' } | ForEach-Object {
  $name, $value = $_ -split '=', 2
  Set-Item -Path "Env:$name" -Value $value
}
mvn spring-boot:run
```

Do not commit `.env`. Use separate secrets and managed database credentials in production. The example database credentials are local development placeholders only.

## API summary

- `POST /api/v1/auth/register` — creates a student account and returns a bearer token.
- `POST /api/v1/auth/login` — authenticates and returns a bearer token.
- `GET /api/v1/students/{studentId}/digital-twin` — returns Twin metrics, missing data, status, learning-gap indicators, risk indicators, and recommendations. A student may read only their own Twin; admin access is role-checked. Teacher assignment access remains to be implemented.
- All non-auth APIs require `Authorization: Bearer <token>`.

## Testing and verification status

No test credentials are committed. Registration is the supported way to create a student account. Verification completed: Maven package build succeeded, all 3 Digital Twin unit tests passed, frontend production build succeeded, and `npm audit` reported 0 known vulnerabilities at install time. MySQL migration execution and the live React-to-API flow are still unverified because MySQL/Docker was not available in the environment. See the implementation status for remaining work.

## Planned next work

CRUD services/controllers for attendance, assessments, skills, projects, certifications, activities, learning progress, and career goals; teacher assignment and admin management; persisted recommendation lifecycle; student/teacher/admin dashboards; API integration and automated tests; security/performance review; production deployment configuration.
