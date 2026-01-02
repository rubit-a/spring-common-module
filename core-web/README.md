# Core Web - 공통 Web 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 Web 관련 유틸 모듈입니다.

## 목표 범위

- 공통 응답 포맷
- 공통 예외 처리
- 공통 검증 유틸/어노테이션
- Web/Jackson 설정 표준화

## 응답 포맷 사용

`core-web`은 응답을 공통 포맷으로 감싸는 기능을 제공합니다.
기본값은 비활성이며 아래 설정으로 활성화할 수 있습니다.

```yaml
core:
  web:
    response:
      enabled: true
      wrapNull: true
      errorEnabled: true
```

응답 포맷:

```json
{
  "success": true,
  "data": { "example": "value" },
  "error": null
}
```

기본적으로 `ApiResponse` 타입은 그대로 반환됩니다.
`String` 응답과 `ResponseEntity` 응답은 감싸지 않습니다.
`wrapNull=false`로 설정하면 `null` 응답은 그대로 반환됩니다.
`errorEnabled=false`로 설정하면 예외 응답 포맷을 비활성화합니다.

예외 응답 포맷:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": {
      "fields": {
        "name": ["must not be blank"]
      }
    }
  }
}
```

## 에러 코드

- `VALIDATION_ERROR` (400)
- `INVALID_REQUEST` (400)
- `MALFORMED_REQUEST` (400)
- `MISSING_PARAMETER` (400)
- `TYPE_MISMATCH` (400)
- `METHOD_NOT_ALLOWED` (405)
- `UNSUPPORTED_MEDIA_TYPE` (415)
- `NOT_FOUND` (404)
- `UNAUTHORIZED` (401)
- `FORBIDDEN` (403)
- `INTERNAL_ERROR` (500)

## 사용 방법

`build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation(project(":core-web"))
}
```

## 빌드

```bash
./gradlew :core-web:build
```
