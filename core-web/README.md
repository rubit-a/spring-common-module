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

## 검증 유틸

커스텀 제약 어노테이션:

- `@EnumValue(enumClass = SampleStatus::class)`
- `@DateRange(min = "2025-01-01", max = "2025-12-31")`
- `@DateRange(min = "2025-01-01", max = "2025-12-31", type = DateRangeType.DATE)`
- `@PatternValue(regexp = "^[a-z]+$")`

`ConstraintViolationException` 발생 시 경로는 최대한 파라미터/필드명으로 정규화됩니다.
`DateRangeType`으로 문자열 입력의 해석 타입을 지정할 수 있습니다.
기본 포맷은 `DATE=yyyy-MM-dd`, `DATE_TIME=yyyy-MM-dd'T'HH:mm:ss`, `TIME=HH:mm:ss` 입니다.
`format` 파라미터는 제공하지 않으며, 타입별 기본 포맷을 사용합니다.

## Web/Jackson 설정

`core-web`은 Jackson/ObjectMapper, JavaTime 포맷, CORS, 공통 컨버터 설정을 제공합니다.
기본값은 비활성이며 아래 설정으로 활성화할 수 있습니다.

```yaml
core:
  web:
    jackson:
      enabled: true
      time-zone: "Asia/Seoul"
      serialization-inclusion: NON_NULL
      date-format: "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
      naming-strategy: SNAKE_CASE
      fail-on-unknown-properties: false
      write-dates-as-timestamps: false
    format:
      enabled: true
      date: "yyyy-MM-dd"
      date-time: "yyyy-MM-dd'T'HH:mm:ss"
      time: "HH:mm:ss"
    cors:
      enabled: true
      path-pattern: "/**"
      allowed-origin-patterns: ["*"]
      allowed-methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
      allowed-headers: ["*"]
      exposed-headers: []
      allow-credentials: false
      max-age: 3600
```

- `format`은 요청 파라미터/PathVariable 변환에 적용됩니다.
- `jackson.enabled=true` + `format.enabled=true`일 때 JavaTime 직렬화 포맷에도 동일 패턴이 적용됩니다.

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
