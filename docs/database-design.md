# Database Design — Digital Twin of a Student

## Conventions

MySQL 8.0+; InnoDB; `BIGINT` generated primary keys; UTC `DATETIME(6)` audit timestamps; `DECIMAL(5,2)` for percentages and marks. Foreign keys are indexed. Required values use `NOT NULL`. No cascade delete from a student or user into academic history. Use soft deactivation for identities and profiles; use explicit retention policy before purging records.

## Schema map

```text
users 1—0..1 students
users 1—0..1 teachers
teachers *—* students (teacher_student_assignments)
students *—* subjects per academic_terms (student_subjects)
students 1—* attendance, assignment_submissions, exam_results,
             student_skills, certifications, projects, activities,
             learning_goals, career_goals, recommendations, mentor_remarks
subjects 1—* assignments, examinations, student_subjects, attendance
assignments 1—* assignment_submissions
examinations 1—* exam_results
learning_goals 1—* learning_progress
```

## Tables and constraints

| Table | Important columns and constraints |
|---|---|
| `users` | `id`, `email` unique, `password_hash`, `role` (`STUDENT`,`TEACHER`,`ADMIN`), `active`, timestamps. Do not return password hash. |
| `students` | `id`, `user_id` unique FK, `student_number` unique, name, department, program, cohort, semester, enrollment date. |
| `teachers` | `id`, `user_id` unique FK, `staff_number` unique, name, department. |
| `teacher_student_assignments` | teacher/student FKs, start/end date, active; unique active pair enforced by service or generated active key. |
| `departments` | unique code and name, active. |
| `academic_terms` | name, academic year, start/end date, active; check start <= end. |
| `subjects` | unique code, name, department FK, credits > 0, active. |
| `student_subjects` | student/subject/term FKs, unique `(student_id, subject_id, term_id)`. |
| `attendance` | student/subject/term FKs, session date, status, optional note and recorder; unique student/subject/term/date. Status is present/absent/late/excused. |
| `assessments` | subject/term FKs, title, type (assignment/internal/exam), max marks > 0, weight 0–100, due date. |
| `assessment_results` | assessment/student FKs, marks 0..max validated against assessment in service, submitted date, grade optional; unique `(assessment_id, student_id)`. |
| `skills` | unique normalized name, category and description. |
| `student_skills` | student/skill FKs, score 0..100, proficiency level, evidence, assessed date; unique student/skill. |
| `certifications` | student FK, name, issuer, issue/expiry date, credential ID/URL; expiry >= issue. |
| `projects` | student FK, name, description, technologies, role, start/end date, status, GitHub/live URLs; end >= start. |
| `activities` | student FK, name, category, description, date, achievement, organization. |
| `learning_goals` | student FK, topic, progress 0..100, start/target/actual dates, status. |
| `learning_progress` | goal FK, progress 0..100, observed date, note. |
| `career_goals` | student FK, target role/domain, target date, status. |
| `career_goal_skills` | career goal/skill FKs, required score 0..100; unique pair. |
| `recommendations` | student FK, category, priority, reason, action, related metric, rule ID/version, generated date, status. |
| `mentor_remarks` | student/teacher FKs, body, visibility, creation timestamp. |
| `audit_events` | actor FK, action, target type/id, timestamp, redacted metadata. Never store credentials or unnecessary personal data. |

## Derived data

The Digital Twin is an API projection, not a duplicated mutable table. Aggregate queries scope by student and term. Recommendations may be persisted for dismissal/history, but each record identifies the rule version and input values used. Weighted score configuration lives in application properties and is documented; it is not copied to every student row.

## Migration policy

Flyway SQL migrations are the schema source of truth. Hibernate schema mode is `validate` outside disposable local development. Avoid `ddl-auto=update` in shared or production databases. Initial migration must include indexes, unique keys, check constraints supported by the target MySQL version, and restrictive foreign-key behavior.
