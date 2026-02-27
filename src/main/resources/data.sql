INSERT IGNORE INTO users(id, email, name, role) VALUES (1, 'student1@example.com', '학생1', 'STUDENT');
INSERT IGNORE INTO users(id, email, name, role) VALUES (2, 'student2@example.com', '학생2', 'STUDENT');
INSERT IGNORE INTO users(id, email, name, role) VALUES (100, 'teacher1@example.com', '강사1', 'INSTRUCTOR');
INSERT IGNORE INTO users(id, email, name, role) VALUES (999, 'admin@example.com', '관리자', 'ADMIN');

INSERT IGNORE INTO courses(id, title, category, instructor_name, capacity)
VALUES (101, '스프링부트 입문', 'IT/외국어', '강사1', 30);
INSERT IGNORE INTO courses(id, title, category, instructor_name, capacity)
VALUES (102, '기초 영어 회화', 'IT/외국어', 'Emma', 25);
INSERT IGNORE INTO courses(id, title, category, instructor_name, capacity)
VALUES (103, '생활 건강 스트레칭', '건강/교양', '김코치', 20);

INSERT IGNORE INTO enrollments(id, user_id, course_id) VALUES (1001, 1, 101);

INSERT IGNORE INTO assignments(id, course_id, title, description, due_date)
VALUES (2001, 101, '1주차 과제', '스프링부트 프로젝트 환경 설정 스크린샷 제출', '2026-03-05');

INSERT IGNORE INTO submissions(id, assignment_id, student_id, content, attachment_path, score, feedback)
VALUES (3001, 2001, 1, '환경설정 완료했습니다. gradle build 성공 캡처 첨부 예정', NULL, 95, '잘했습니다!');

INSERT IGNORE INTO notices(id, title, content, author_name)
VALUES (4001, 'LMS 오픈 안내', 'LMS 시범 운영을 시작합니다. 문의는 관리자에게 주세요.', '관리자');

INSERT IGNORE INTO qna_questions(id, student_id, title, content)
VALUES (5001, 1, '과제 제출이 안됩니다', '마감 전인데 제출 버튼이 비활성처럼 보입니다.');

INSERT IGNORE INTO qna_answers(id, question_id, responder_name, content)
VALUES (6001, 5001, '강사1', '브라우저 새로고침 후 다시 시도해보세요. 계속 안되면 캡처 부탁해요.');

INSERT IGNORE INTO attendance_records(id, enrollment_id, attendance_date, status)
VALUES (7001, 1001, CURRENT_DATE, 'PRESENT');
