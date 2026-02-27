# Architecture Design Document

## 1. 개요
본 LMS는 Spring Boot 기반 계층형 아키텍처(Layered Architecture)로 구성한다.

## 2. 기술 스택
- Java 17
- Spring Boot
- Thymeleaf
- JdbcTemplate (주요 데이터 접근)
- JPA (검증 보조)
- MySQL
- Gradle

## 3. 상위 아키텍처 구성
- 표현 계층(Presentation): Thymeleaf View + Controller
- 응용 계층(Application): Service(비즈니스 규칙, 검증, 권한)
- 데이터 접근 계층(Data Access): JdbcTemplate Repository + RowMapper
- 데이터 계층(Data): MySQL (schema.sql, data.sql)

## 4. 아키텍처 다이어그램 (요약)
```text
[Browser]
   |
   v
[Controller] -> [Interceptor(Auth/Role)]
   |
   v
[Service (Business Rules)]
   |
   v
[Repository (JdbcTemplate)]
   |
   v
[MySQL]
```

## 5. 패키지 구조(대표)
- `controller/`: 웹 요청 처리 및 화면 라우팅
- `api/`: API 엔드포인트 및 DTO
- `service/`: 비즈니스 로직
- `repository/`: SQL 기반 영속성 처리
- `domain/`: 도메인 모델
- `common/`: 인증/검증/예외처리/인터셉터
- `config/`: 웹 설정 및 인터셉터 등록
- `templates/fragments/`: 공통 화면 조각(헤더/내비게이션)

## 6. 요청 처리 흐름
1. 클라이언트 요청이 Controller로 진입
2. Interceptor에서 인증/권한 검증
3. Service에서 업무 규칙 적용
4. Repository에서 JdbcTemplate로 SQL 실행
5. 결과를 View 렌더링 또는 API 응답으로 반환

## 7. 보안 및 접근제어
- 세션 기반 로그인
- 인터셉터 + 서비스 레벨 권한 검증
- 관리자 보호 경로 분리

## 8. 데이터 초기화 전략
- `schema.sql`: 스키마 생성
- `data.sql`: 샘플 데이터 초기화(local)
- 프로파일 기반 설정 분리(`application-local.yaml`, `application-prod.yaml`)

## 9. 예외 처리
- 웹 전역 예외 처리기(WebExceptionHandler)
- API 전역 예외 처리기(ApiExceptionHandler)

## 10. 배포 고려사항
- DB 설정값은 환경변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)로 외부화
- local/prod 프로파일 분리 유지
- 업로드 디렉터리 권한 및 경로 사전 점검
