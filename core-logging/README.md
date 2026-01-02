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

## 빌드

```bash
./gradlew :core-logging:build
```
