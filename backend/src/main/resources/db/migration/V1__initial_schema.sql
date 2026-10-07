CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT chk_user_role CHECK (role IN ('STUDENT','TEACHER','ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE departments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(160) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  student_number VARCHAR(80) NOT NULL UNIQUE,
  full_name VARCHAR(180) NOT NULL,
  department_id BIGINT NULL,
  program VARCHAR(160) NULL,
  cohort VARCHAR(80) NULL,
  semester INT NULL,
  enrollment_date DATE NULL,
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_student_department FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT chk_student_semester CHECK (semester IS NULL OR semester BETWEEN 1 AND 16)
) ENGINE=InnoDB;

CREATE TABLE teachers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  staff_number VARCHAR(80) NOT NULL UNIQUE,
  full_name VARCHAR(180) NOT NULL,
  department_id BIGINT NULL,
  CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_teacher_department FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

CREATE TABLE teacher_student_assignments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  assigned_on DATE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_tsa_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id),
  CONSTRAINT fk_tsa_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT uq_tsa_pair UNIQUE (teacher_id, student_id)
) ENGINE=InnoDB;

CREATE TABLE academic_terms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  academic_year VARCHAR(20) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT chk_term_dates CHECK (end_date >= start_date),
  CONSTRAINT uq_term UNIQUE (name, academic_year)
) ENGINE=InnoDB;

CREATE TABLE subjects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL UNIQUE,
  name VARCHAR(180) NOT NULL,
  department_id BIGINT NULL,
  credits DECIMAL(4,1) NOT NULL DEFAULT 1,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_subject_department FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT chk_subject_credits CHECK (credits > 0)
) ENGINE=InnoDB;

CREATE TABLE student_subjects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  term_id BIGINT NOT NULL,
  CONSTRAINT fk_ss_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_ss_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
  CONSTRAINT fk_ss_term FOREIGN KEY (term_id) REFERENCES academic_terms(id),
  CONSTRAINT uq_student_subject_term UNIQUE (student_id, subject_id, term_id)
) ENGINE=InnoDB;

CREATE TABLE attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  term_id BIGINT NOT NULL,
  session_date DATE NOT NULL,
  status VARCHAR(12) NOT NULL,
  note VARCHAR(500) NULL,
  recorded_by BIGINT NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_att_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
  CONSTRAINT fk_att_term FOREIGN KEY (term_id) REFERENCES academic_terms(id),
  CONSTRAINT fk_att_recorder FOREIGN KEY (recorded_by) REFERENCES users(id),
  CONSTRAINT uq_att_session UNIQUE (student_id, subject_id, term_id, session_date),
  CONSTRAINT chk_att_status CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED')),
  INDEX idx_att_student_date (student_id, session_date)
) ENGINE=InnoDB;

CREATE TABLE assessments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subject_id BIGINT NOT NULL,
  term_id BIGINT NOT NULL,
  title VARCHAR(180) NOT NULL,
  assessment_type VARCHAR(20) NOT NULL,
  max_marks DECIMAL(7,2) NOT NULL,
  weight_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
  due_date DATE NULL,
  CONSTRAINT fk_assess_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
  CONSTRAINT fk_assess_term FOREIGN KEY (term_id) REFERENCES academic_terms(id),
  CONSTRAINT chk_assess_marks CHECK (max_marks > 0),
  CONSTRAINT chk_assess_weight CHECK (weight_percent BETWEEN 0 AND 100),
  CONSTRAINT chk_assess_type CHECK (assessment_type IN ('ASSIGNMENT','INTERNAL','EXAM'))
) ENGINE=InnoDB;

CREATE TABLE assessment_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  assessment_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  marks DECIMAL(7,2) NOT NULL,
  grade VARCHAR(10) NULL,
  recorded_by BIGINT NOT NULL,
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_result_assessment FOREIGN KEY (assessment_id) REFERENCES assessments(id),
  CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_result_recorder FOREIGN KEY (recorded_by) REFERENCES users(id),
  CONSTRAINT uq_assessment_student UNIQUE (assessment_id, student_id),
  CONSTRAINT chk_result_marks CHECK (marks >= 0)
) ENGINE=InnoDB;

CREATE TABLE skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL UNIQUE,
  category VARCHAR(80) NOT NULL,
  description VARCHAR(500) NULL
) ENGINE=InnoDB;

CREATE TABLE student_skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  evidence VARCHAR(1000) NULL,
  assessed_on DATE NOT NULL,
  assessed_by BIGINT NULL,
  CONSTRAINT fk_skill_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_skill_skill FOREIGN KEY (skill_id) REFERENCES skills(id),
  CONSTRAINT fk_skill_assessor FOREIGN KEY (assessed_by) REFERENCES users(id),
  CONSTRAINT uq_student_skill UNIQUE (student_id, skill_id),
  CONSTRAINT chk_skill_score CHECK (score BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE certifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  name VARCHAR(180) NOT NULL,
  issuer VARCHAR(180) NOT NULL,
  issue_date DATE NOT NULL,
  expiry_date DATE NULL,
  credential_id VARCHAR(120) NULL,
  credential_url VARCHAR(500) NULL,
  CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT chk_cert_dates CHECK (expiry_date IS NULL OR expiry_date >= issue_date)
) ENGINE=InnoDB;

CREATE TABLE projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  name VARCHAR(180) NOT NULL,
  description TEXT NULL,
  technologies VARCHAR(1000) NULL,
  role VARCHAR(120) NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  status VARCHAR(20) NOT NULL,
  github_url VARCHAR(500) NULL,
  live_url VARCHAR(500) NULL,
  CONSTRAINT fk_project_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT chk_project_dates CHECK (end_date IS NULL OR end_date >= start_date),
  CONSTRAINT chk_project_status CHECK (status IN ('PLANNED','IN_PROGRESS','COMPLETED'))
) ENGINE=InnoDB;

CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  name VARCHAR(180) NOT NULL,
  category VARCHAR(100) NOT NULL,
  description VARCHAR(1000) NULL,
  activity_date DATE NOT NULL,
  achievement VARCHAR(300) NULL,
  organization VARCHAR(180) NULL,
  CONSTRAINT fk_activity_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB;

CREATE TABLE learning_goals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  topic VARCHAR(180) NOT NULL,
  progress DECIMAL(5,2) NOT NULL DEFAULT 0,
  start_date DATE NOT NULL,
  target_date DATE NOT NULL,
  actual_completion_date DATE NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_goal_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT chk_goal_progress CHECK (progress BETWEEN 0 AND 100),
  CONSTRAINT chk_goal_dates CHECK (target_date >= start_date),
  CONSTRAINT chk_goal_status CHECK (status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED'))
) ENGINE=InnoDB;

CREATE TABLE learning_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  goal_id BIGINT NOT NULL,
  progress DECIMAL(5,2) NOT NULL,
  observed_on DATE NOT NULL,
  note VARCHAR(500) NULL,
  CONSTRAINT fk_progress_goal FOREIGN KEY (goal_id) REFERENCES learning_goals(id),
  CONSTRAINT chk_progress_value CHECK (progress BETWEEN 0 AND 100),
  INDEX idx_progress_goal_date (goal_id, observed_on)
) ENGINE=InnoDB;

CREATE TABLE career_goals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  target_role VARCHAR(180) NOT NULL,
  preferred_domain VARCHAR(120) NULL,
  target_date DATE NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT fk_career_student FOREIGN KEY (student_id) REFERENCES students(id)
) ENGINE=InnoDB;

CREATE TABLE career_goal_skills (
  career_goal_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  required_score DECIMAL(5,2) NOT NULL DEFAULT 60,
  PRIMARY KEY (career_goal_id, skill_id),
  CONSTRAINT fk_cgs_goal FOREIGN KEY (career_goal_id) REFERENCES career_goals(id),
  CONSTRAINT fk_cgs_skill FOREIGN KEY (skill_id) REFERENCES skills(id),
  CONSTRAINT chk_cgs_score CHECK (required_score BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE recommendations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  category VARCHAR(40) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  reason VARCHAR(1000) NOT NULL,
  recommendation VARCHAR(1000) NOT NULL,
  related_metric VARCHAR(80) NOT NULL,
  rule_id VARCHAR(80) NOT NULL,
  rule_version INT NOT NULL DEFAULT 1,
  generated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  CONSTRAINT fk_recommend_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT chk_recommend_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
  CONSTRAINT chk_recommend_status CHECK (status IN ('ACTIVE','DISMISSED','COMPLETED')),
  INDEX idx_recommend_student_date (student_id, generated_at)
) ENGINE=InnoDB;

CREATE TABLE mentor_remarks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  body VARCHAR(2000) NOT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_remark_student FOREIGN KEY (student_id) REFERENCES students(id),
  CONSTRAINT fk_remark_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id)
) ENGINE=InnoDB;

CREATE TABLE audit_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor_user_id BIGINT NULL,
  action VARCHAR(80) NOT NULL,
  target_type VARCHAR(80) NOT NULL,
  target_id BIGINT NULL,
  metadata JSON NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id),
  INDEX idx_audit_target (target_type, target_id),
  INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;
