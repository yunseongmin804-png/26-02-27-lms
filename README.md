# LMS 과제 제출용 가이드

Spring Boot + Thymeleaf + MySQL + JdbcTemplate 기반 LMS입니다.

## 1) 실행 방법

```bash
cd ~/projects/lms
./gradlew bootRun
```

접속:
- `http://localhost:8080/login`

기본 샘플 계정(사용자 선택 로그인):
- 학생1 / 학생2 / 강사1 / 관리자

---

## 2) 기술 스택

- Java 17+
- Spring Boot 3.x
- Thymeleaf
- JdbcTemplate (핵심 Repository)
- JPA (ddl-auto=validate 검증용)
- MySQL

---

## 3) 구현 기능 요약

### 인증/권한
- 세션 로그인/로그아웃
- 역할: STUDENT / INSTRUCTOR / ADMIN
- 인터셉터 + 서비스 레벨 권한 검증

### 사용자/강의/수강
- 사용자 목록/등록(관리자)
- 강의 목록/검색/상세/수정/삭제
- 수강 신청/취소
- 정원 초과 방지, 중복 신청 방지

### 과제/제출/채점
- 과제 등록/조회/필터(강의별)
- 과제 제출/재제출
- 마감일 이후 제출 차단
- 채점(점수/피드백)
- 파일 첨부 업로드/다운로드

### 공지/Q&A
- 공지사항 CRUD + 검색/정렬/페이지네이션
- Q&A 질문/답변(다중 답변) + 검색/정렬/페이지네이션

### 출석
- 출석 기록(PRESENT/LATE/ABSENT)
- 출석 수정/삭제
- 강의별 일괄 기록
- 학생별 일괄 편집(업서트)
- 출석률 계산(학생/관리자)
- CSV 다운로드(일반/기간/결석/요약/TOP N)

### 관리자 대시보드
- 핵심 지표 카드
- 결석 위험(기준% 동적 설정)
- 위험 학생 목록
- 결석 TOP 10
- 감사로그 조회
- 공지 초안/자동등록 지원

### UI/UX 개선 사항
- 공통 헤더/내비게이션(fragment) 기반 화면 일관성 강화
- 역할별 홈 퀵링크 문구 분기
- 출석 상태 한글 라벨 표시(출석/지각/결석)
- 삭제 액션 확인창 + 경고 스타일 적용

---

## 4) 데모 시나리오 (발표용)

1. 로그인(관리자)
2. 홈 대시보드 지표 확인
3. 강의 목록 검색 + 강의 상세 이동
4. 수강신청 등록/취소 시연
5. 과제 등록 → 제출(학생) → 채점(강사/관리자)
6. 출석 관리 (일괄 기록/학생별 편집/CSV 다운로드)
7. 관리자 대시보드 (결석 위험 기준 조절, 공지 초안/자동등록)

---

## 5) 제출 전 체크리스트

- [ ] `./gradlew test` 성공
- [ ] `./gradlew bootRun` 정상 기동
- [ ] 로그인/권한 흐름 확인
- [ ] 과제 제출/채점 확인
- [ ] 출석 CSV 다운로드 확인
- [ ] 관리자 대시보드 동작 확인

---

## 6) 실행 트러블슈팅

### 6.1 DB 인증 실패
에러 예시: `Access denied for user 'root'@'localhost' (using password: NO)`

```bash
export DB_USERNAME=root
export DB_PASSWORD='내_mysql_비밀번호'
./gradlew bootRun
```

### 6.2 포트 충돌
기본 8080 포트가 사용 중이면:

```bash
./gradlew bootRun --args='--server.port=8081'
# 또는 8083
./gradlew bootRun --args='--server.port=8083'
```

### 6.3 DB 초기화

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS lms; CREATE DATABASE lms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
./gradlew bootRun
```

---

## 7) 문서
- `LMS_Project_Performance_Plan.md`
- `Requirements_Specification.md`
- `Architecture_Design_Document.md`
- `UI_UX_Design_Specification.md`
- `Database_Design_Document.md`
- `plan.md` (화면 캡처 모음)
