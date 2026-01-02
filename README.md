# Spring Common Module

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 모듈 모음입니다.

## 프로젝트 구조

```
spring-common-module/
├── core-logging/           # 공통 로깅 유틸 모듈
├── core-security/          # 인증 공통 라이브러리 (JWT/Session)
├── core-web/               # 공통 Web 유틸 모듈
└── core-test/               # core-security 사용 예제 프로젝트
```

## 모듈 소개

### 1. core-security

다른 Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 인증 라이브러리입니다.

**주요 기능:**
- JWT Access Token 및 Refresh Token 생성
- JWT 토큰 검증
- Spring Security 통합 (자동 인증 필터)
- Spring Boot Auto Configuration 지원
- 인증 모드 선택 지원 (`jwt` 또는 `session`)

**자세한 내용:** [core-security/README.md](core-security/README.md)

### 2. core-web

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 Web 관련 유틸 모듈입니다.

**자세한 내용:** [core-web/README.md](core-web/README.md)

### 3. core-logging

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 로깅 유틸 모듈입니다.

**자세한 내용:** [core-logging/README.md](core-logging/README.md)

### 4. core-test

`core-security` 모듈을 활용하는 실제 웹 애플리케이션 예제입니다.

**주요 기능:**
- JWT 기반 로그인 API
- 인증이 필요한 보호된 엔드포인트
- 역할 기반 접근 제어 (RBAC)
- 공개 엔드포인트

**자세한 내용:** [core-test/README.md](core-test/README.md)

## 빠른 시작

### 1. 전체 프로젝트 빌드

```bash
cd spring-common-module
./core-security/gradlew build
./gradlew :core-logging:build
./gradlew :core-web:build
```

### 2. core-test 애플리케이션 실행

```bash
cd core-test
../core-security/gradlew bootRun
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

### 3. API 테스트

로그인하여 토큰 받기:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

토큰으로 보호된 엔드포인트 호출:
```bash
TOKEN="여기에_받은_토큰_붙여넣기"
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

## 멀티 모듈 구성

이 프로젝트는 Gradle 멀티 모듈로 구성되어 있습니다.

### settings.gradle.kts
```kotlin
rootProject.name = "spring-common-module"

include("core-security")
include("core-logging")
include("core-web")
include("core-test")
```

### 모듈 간 의존성

`core-test`은 `core-security`를 의존성으로 사용합니다:

```kotlin
// core-test/build.gradle.kts
dependencies {
    implementation(project(":core-security"))
    // ...
}
```

## 새 프로젝트에서 core-security 사용하기

### 1. 의존성 추가

같은 멀티 모듈 프로젝트 내에서:
```kotlin
dependencies {
    implementation(project(":core-security"))
}
```

Maven 저장소에 배포한 경우:
```kotlin
dependencies {
    implementation("rubit:core-security:0.0.1-SNAPSHOT")
}
```

### 2. application.yml 설정

```yaml
auth:
  mode: jwt

jwt:
  secret-key: your-secret-key-here-minimum-256-bits-long
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000
  issuer: your-app-name
```

### 3. JwtTokenProvider 사용

```kotlin
@RestController
class JwtAuthController(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        val token = jwtTokenProvider.generateAccessToken(
            username = request.username,
            authorities = listOf("ROLE_USER")
        )
        return TokenResponse(token)
    }
}
```

### 4. Security 설정 (선택사항)

```kotlin
@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

## 기술 스택

- **언어:** Kotlin 2.2.21
- **프레임워크:** Spring Boot 4.0.1
- **보안:** Spring Security
- **JWT:** jjwt 0.12.6
- **빌드 도구:** Gradle 9.2.1
- **Java 버전:** 21

## 개발 환경

### 필수 요구사항

- JDK 21 이상
- Gradle 9.2.1 이상 (또는 포함된 gradlew 사용)

### 빌드 명령어

```bash
# 전체 프로젝트 빌드
./core-security/gradlew build
./gradlew :core-logging:build
./gradlew :core-web:build

# 특정 모듈만 빌드
./core-security/gradlew :core-security:build
./gradlew :core-logging:build
./gradlew :core-web:build
./core-security/gradlew :core-test:build

# 테스트 실행
./core-security/gradlew test

# 클린 빌드
./core-security/gradlew clean build
```

## 라이선스

이 프로젝트는 비공개 프로젝트입니다.

## 참고 자료

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
