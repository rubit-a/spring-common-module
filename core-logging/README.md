# Core Logging - 공통 로깅 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 로깅 유틸 모듈입니다.

## 기능

- 요청 ID 생성/전파
- MDC에 요청 ID 주입
- 응답 헤더에 요청 ID 설정

## 설정

```yaml
core:
  logging:
    request-id:
      enabled: true
      header: X-Request-Id
      mdc-key: requestId
      generate-if-missing: true
```

## 사용 방법

`build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation(project(":core-logging"))
}
```

## 로그 패턴 예시

Logback 콘솔 패턴에서 MDC 값을 출력하려면 `%X{requestId}`를 사용합니다.

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{requestId}] %logger{36} - %msg%n"
```

로그 예시:

```
2025-01-02 10:15:30.123 INFO  [req-123] rubit.api.SampleController - handled request
```

## Logback 설정 예시

모듈에 기본 예시 파일을 포함했습니다.

- `core-logging/src/main/resources/logback-spring.xml`

필요에 따라 애플리케이션에서 덮어쓰거나 수정할 수 있습니다.

## 빌드

```bash
./gradlew :core-logging:build
```
