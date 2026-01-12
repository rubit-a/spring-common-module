# Core Docs - 공통 API 문서 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 OpenAPI/Swagger 설정 모듈입니다.

## 기능

- OpenAPI 기본 Info 구성
- 서버 URL 설정
- JWT Bearer 인증 스키마 등록

## 사용 방법

`build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation("rubit:core-docs:1.0.0")
}
```

## 설정

```yaml
core:
  docs:
    enabled: true
    title: "My API"
    description: "Service API documentation"
    version: "v1"
    server-url: "https://api.example.com"
    security:
      enabled: true
      scheme-name: "BearerAuth"
      scheme: "bearer"
      bearer-format: "JWT"
      description: "JWT access token"
```

Springdoc 기본 엔드포인트:
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

## 경로 변경

기본 경로를 변경하려면 아래 설정을 추가하세요:

```yaml
springdoc:
  api-docs:
    path: /api/docs
  swagger-ui:
    path: /api/swagger-ui
```

예시 경로:
- OpenAPI JSON: `/api/docs`
- Swagger UI: `/api/swagger-ui`

## 빌드

```bash
./gradlew :core-docs:build
```
