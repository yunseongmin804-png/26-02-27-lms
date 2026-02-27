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
- MySQL (개발 테스트는 H2)

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
- CSV 다운로드:
  - 일반 CSV
  - 기간 CSV
  - 결석만 CSV
  - 출석률 요약 CSV
  - 결석 TOP N CSV

### 관리자 대시보드
- 핵심 지표 카드
- 결석 위험(기준% 동적 설정)
- 결석 위험 학생 목록
- 결석 TOP 10
- 감사로그 조회
- 위험학생 공지/메시지 지원:
  - 공지 초안 채우기
  - 과목별 공지 초안(.md)
  - 과목별 공지 자동등록(중복 방지)
  - 경고 메시지 템플릿(.txt)

---

## 4) 데모 시나리오 (발표용)

1. 로그인(관리자)
2. 홈 대시보드 지표 확인
3. 강의 목록 검색 + 강의 상세 이동
4. 수강신청 등록/취소 시연
5. 과제 등록 → 제출(학생) → 채점(강사/관리자)
6. 출석 관리
   - 일괄 기록
   - 학생별 일괄 편집
   - CSV 다운로드
7. 관리자 대시보드
   - 결석 위험 기준 조절
   - 공지 초안/자동등록 버튼 시연

---

## 5) 제출 전 체크리스트

- [ ] `./gradlew test` 성공
- [ ] `./gradlew bootRun` 정상 기동
- [ ] 로그인/권한 흐름 확인
- [ ] 과제 제출/채점 확인
- [ ] 출석 CSV 다운로드 확인
- [ ] 관리자 대시보드 버튼 동작 확인

---

## 6) 참고

DB 스키마 변경으로 충돌이 나면 개발용 초기화:

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS lms; CREATE DATABASE lms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

그 다음 `./gradlew bootRun`.
