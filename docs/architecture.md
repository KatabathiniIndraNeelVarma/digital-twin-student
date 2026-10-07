# Architecture and Security Baseline

The platform is a modular monolith. React accesses domain behavior only through `/api/v1`. Spring controllers validate request DTOs, services own authorization/business rules, repositories own persistence, and Flyway controls schema changes. The Twin response is a read projection aggregated from source tables.

Authentication uses BCrypt and signed, expiring JWTs. API routes are stateless. Students must be scoped to their linked profile. Administrative endpoints require the administrative role; teacher endpoints require a current teacher/student assignment. CORS allows only the configured frontend origin. Secrets are environment variables. Detailed role and assignment enforcement is mandatory as each resource controller is added.

## Digital Twin rules

- Weighted score: academic 0.35, attendance 0.15, skills 0.20, learning progress 0.15, projects 0.10, activities 0.05.
- Only available dimensions contribute; weights are renormalized and missing dimensions are returned to the client.
- Scores are bounded to 0–100 and rounded to two decimals.
- Classifications are configurable and documented in the README and `application.yml`.
- Learning gaps are called performance-based indicators, not diagnoses.
- Recommendations state the triggering metric, reason, and suggested action.
- Project/activity participation uses a provisional count-based score and must be reviewed with the institution before deployment.
