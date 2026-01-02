# Core Logging - 공통 로깅 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 로깅 유틸 모듈입니다.

## 기능

- 요청 ID 생성/전파
- MDC에 요청 ID 주입
- 응답 헤더에 요청 ID 설정
- 분산 추적 헤더(traceparent/B3)에서 traceId/spanId 추출 후 MDC 주입
- AOP 기반 실행 시간 로깅

## 설정

```yaml
core:
  logging:
    request-id:
      enabled: true
      header: X-Request-Id
      mdc-key: requestId
      generate-if-missing: true
    trace:
      enabled: true
      mdc-trace-id-key: traceId
      mdc-span-id-key: spanId
      generate-if-missing: true
    aop:
      enabled: false
      slow-threshold-ms: 0
      log-args: false
      log-result: false
```

## 사용 방법

`build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation(project(":core-logging"))
}
```

## 로그 패턴 예시

Logback 콘솔 패턴에서 MDC 값을 출력하려면 `%X{requestId}`, `%X{traceId}`, `%X{spanId}`를 사용합니다.

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [req=%X{requestId} trace=%X{traceId} span=%X{spanId}] %logger{36} - %msg%n"
```

로그 예시:

```

## AOP 로깅 사용

실행 시간 로깅이 필요한 메서드/클래스에 `@LogExecutionTime`을 붙입니다.

```kotlin
import rubit.corelogging.aop.LogExecutionTime

@LogExecutionTime
fun doSomething() {
    // ...
}
```
2025-01-02 10:15:30.123 INFO  [req-123 trace=4bf92f3577b34da6a3ce929d0e0e4736 span=00f067aa0ba902b7] rubit.api.SampleController - handled request
```

## 분산 추적 헤더 지원

- W3C Trace Context: `traceparent`
- B3 Single: `b3`
- B3 Multi: `X-B3-TraceId`, `X-B3-SpanId`

## Logback 설정 예시

모듈에 기본 예시 파일을 포함했습니다.

- `core-logging/src/main/resources/logback-spring.xml`

필요에 따라 애플리케이션에서 덮어쓰거나 수정할 수 있습니다.

## 빌드

```bash
./gradlew :core-logging:build
```
