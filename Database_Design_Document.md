# Database Design Document

## 1. 개요
LMS 데이터베이스는 MySQL 기반이며 `schema.sql`, `data.sql`로 초기화한다.

## 2. 설계 목표
- 학습관리 핵심 엔터티의 정규화
- 사용자 역할/강의 라이프사이클 지원
- 제출/채점/출석/공지/Q&A 이력 관리
- 관리자 대시보드용 조회/통계 지원

## 3. 핵심 엔터티(개념)
- User
- Course
- Enrollment
- Assignment
- Submission
- AttendanceRecord
- Notice
- QnaQuestion
- QnaAnswer
- AuditLog

## 4. 관계 요약
- User (1) — (N) Enrollment
- Course (1) — (N) Enrollment
- Course (1) — (N) Assignment
- Assignment (1) — (N) Submission
- User (1) — (N) Submission
- Enrollment (1) — (N) AttendanceRecord
- QnaQuestion (1) — (N) QnaAnswer

## 5. 주요 테이블 요약 (실제 스키마 기준)
- `users(id, email, name, role)`
- `courses(id, title, category, instructor_name, capacity, created_at)`
- `enrollments(id, user_id, course_id, enrolled_at)`
- `assignments(id, course_id, title, description, due_date, created_at)`
- `submissions(id, assignment_id, student_id, content, attachment_path, score, feedback, submitted_at)`
- `notices(id, title, content, author_name, created_at)`
- `qna_questions(id, student_id, title, content, created_at)`
- `qna_answers(id, question_id, responder_name, content, created_at)`
- `audit_logs(id, actor_id, actor_name, actor_role, action, target_type, target_id, detail, created_at)`
- `attendance_records(id, enrollment_id, attendance_date, status, created_at)`

## 6. 주요 제약조건
- `uk_enrollments_user_course` : `(user_id, course_id)` 유니크
- `uk_submission_assignment_student` : `(assignment_id, student_id)` 유니크
- `uk_attendance_enrollment_date` : `(enrollment_id, attendance_date)` 유니크
- 외래키 무결성: enrollments, assignments, submissions, qna, attendance_records

## 7. 인덱스 전략(권장)
- Enrollment: `(user_id, course_id)` 유니크 인덱스
- Attendance: `(enrollment_id, attendance_date)` 인덱스
- Submission: `(assignment_id, student_id)` 인덱스
- Notice/Q&A: `created_at` 정렬 인덱스

## 8. 초기화 및 환경
- local profile: SQL 초기화 모드 `always`
- production profile: SQL 초기화 모드 `never`
- DB 접속 정보: 환경변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)

## 9. 보안 및 운영
- DB 비밀번호 하드코딩 금지
- 운영 환경은 최소 권한 DB 계정 사용
- 운영 스키마 변경 전 백업 필수

## 10. 참고 파일
- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-prod.yaml`
