# Core Web - 공통 Web 모듈

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 Web 관련 유틸 모듈입니다.

## 목표 범위

- 공통 응답 포맷
- 공통 예외 처리
- 공통 검증 유틸/어노테이션
- Web/Jackson 설정 표준화

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
