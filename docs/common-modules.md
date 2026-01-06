# 공통 모듈 후보 정리

이 문서는 `core-security`처럼 공통화/라이브러리화하기 좋은 기능들을 정리합니다.
프로젝트마다 반복되거나, 보안/운영 품질에 직접 영향을 주는 영역을 우선 대상으로 삼습니다.
단일 분류마다 별도 모듈로 분리하기보다 **응집도/의존성/변경 주기** 기준으로 묶는 방향을 권장합니다.

## 권장 모듈 구성 (응집도/의존성 기준)

### 핵심 묶음 (적은 의존성, 대부분의 서비스가 공통 사용)
#### core-web
- 범위: 공통 예외/응답/검증/웹 설정
- 포함 예시
  - `ApiError`, 에러 코드 enum, 전역 예외 핸들러
  - `ApiResponse<T>`, 페이징 응답
  - 커스텀 `@Constraint` 및 검증 유틸
  - CORS 설정, `ObjectMapper` 옵션, 타임존 정책

#### core-logging
- 범위: 로깅/추적 표준화
- 포함 예시
  - 요청 ID, MDC 관리
  - 구조적 로깅(JSON), 요청/응답 로깅
  - 성능 측정 AOP

#### core-security
- 범위: 보안 유틸/기본 정책
- 포함 예시
  - 역할/권한 상수, 인증 컨텍스트 헬퍼
  - 보안 헤더, IP allow/deny, rate limiting
- 참고: OAuth2 로그인/Provider 연동은 의존성이 크므로 `core-security-oauth2`로 분리 권장

### 선택 묶음 (의존성 크고 서비스별 선택 사항)
#### core-http
- 범위: 외부 API 호출 표준화
- 포함 예시: `RestClient`/`WebClient` 기본 설정, 타임아웃/리트라이, 로깅 필터

#### core-data
- 범위: DB/JPA 공통 인프라
- 포함 예시: BaseEntity, JPA Auditing, 공통 컬럼/인덱스 가이드

#### core-cache
- 범위: 캐시 정책 통일
- 포함 예시: CacheManager 설정, 캐시 키 규약, 캐시 무효화 유틸

#### core-messaging
- 범위: 메시징 설정 표준화
- 포함 예시: Kafka/Rabbit 기본 설정, 공통 프로듀서/컨슈머 템플릿

#### core-storage
- 범위: 파일/스토리지 공통화
- 포함 예시: S3 업로더, 파일 메타데이터 관리, 보안 체크

#### core-notify
- 범위: 알림 채널 통합
- 포함 예시: 이메일/슬랙/푸시 템플릿, 실패 재시도 정책

## 모듈화 우선순위 가이드

### 빠르게 효과가 큰 영역
- core-web
- core-logging
- core-security

### 서비스 수가 많을수록 가치가 큰 영역
- core-http
- core-data
- core-messaging

## 다음 단계 제안

- 어떤 모듈부터 만들지 우선순위를 정합니다.
- 모듈 이름, 스코프, 기본 API를 정의합니다.
- 현재 `core-security`와의 의존 관계를 점검합니다.
