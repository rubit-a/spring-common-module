# Spring Common Module

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 모듈 모음입니다.

## 프로젝트 구조

```
spring-common-module/
├── core-data/              # 공통 데이터/JPA 인프라 모듈
├── core-logging/           # 공통 로깅 유틸 모듈
├── core-security/          # 인증 공통 라이브러리 (JWT/Session)
├── core-security-oauth2/   # OAuth2 로그인 + 자체 JWT 발급
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

### 2. core-security-oauth2

OAuth2 로그인 후 자체 JWT를 발급하는 공통 모듈입니다.

**자세한 내용:** [core-security-oauth2/README.md](core-security-oauth2/README.md)

### 3. core-web

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 Web 관련 유틸 모듈입니다.

**자세한 내용:** [core-web/README.md](core-web/README.md)

### 4. core-logging

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 로깅 유틸 모듈입니다.

**자세한 내용:** [core-logging/README.md](core-logging/README.md)

### 5. core-data

Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 DB/JPA 인프라 모듈입니다.

**자세한 내용:** [core-data/README.md](core-data/README.md)

### 6. core-test

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
./gradlew :core-security-oauth2:build
./gradlew :core-data:build
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
include("core-security-oauth2")
include("core-logging")
include("core-web")
include("core-data")
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

## GitHub Packages 배포 및 의존 설정

GitHub Packages Maven registry를 통해 공통 모듈을 배포하고 의존합니다.

### 1. 저장소/인증 정보 설정

`gradle.properties` 또는 환경 변수로 설정합니다.

`gradle.properties`:
```properties
githubRepository=OWNER/REPO
gpr.user=your-github-username
gpr.key=your-github-token
```

환경 변수:
```bash
export GITHUB_REPOSITORY=OWNER/REPO
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-github-token
```

> PAT 권한: `read:packages`, `write:packages`

### 1-1. 안전한 인증 설정 (권장)

토큰은 **프로젝트 파일에 하드코딩하지 말고** 아래 방식으로 관리하세요.

- 로컬: `~/.gradle/gradle.properties`
```properties
githubRepository=OWNER/REPO
gpr.user=your-github-username
gpr.key=your-github-token
```

- CI(GitHub Actions): `GITHUB_TOKEN`, `GITHUB_ACTOR` 자동 사용

- 로컬 래퍼 스크립트(환경변수만 사용)
  - `scripts/gradle-with-env.sh`에 값을 입력한 뒤 사용
  - 예: `./scripts/gradle-with-env.sh :core-security:publish`

> `gpr.key`는 `read:packages` 권한이 필요하고, publish 시 `write:packages` 권한이 필요합니다.

### 2. 모듈 배포

```bash
./gradlew :core-logging:publish
./gradlew :core-security:publish
./gradlew :core-security-oauth2:publish
./gradlew :core-data:publish
./gradlew :core-web:publish
```

### 3. core-test 실행 (GitHub Packages 의존)

```bash
./gradlew :core-test:bootRun
```

로컬 모듈을 직접 참조하려면:
```bash
./gradlew :core-test:bootRun -PuseLocalModules=true
```

### 4. 외부 프로젝트에서 사용

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("rubit:core-logging:0.0.1-SNAPSHOT")
    implementation("rubit:core-security:0.0.1-SNAPSHOT")
    implementation("rubit:core-security-oauth2:0.0.1-SNAPSHOT")
    implementation("rubit:core-data:0.0.1-SNAPSHOT")
    implementation("rubit:core-web:0.0.1-SNAPSHOT")
}
```

> 저장소 주소를 직접 지정하려면 `githubPackagesUrl` Gradle 속성을 사용하세요.

### GitHub Packages 주소 변경 시 설정

GitHub repository가 변경된 경우, `gradle.properties`에서 덮어쓰면 됩니다.

Repository 지정:
```properties
githubRepository=OWNER/REPO
```

URL 직접 지정:
```properties
githubPackagesUrl=https://maven.pkg.github.com/OWNER/REPO
```

인증 정보 변경:
```properties
gpr.user=사용자
gpr.key=토큰
```

### build 시 자동 배포/의존성 갱신

`gradle.properties`에서 아래 설정을 켜면 `build` 실행 시 자동으로 `publish`가 함께 수행되고,
SNAPSHOT 의존성은 매번 원격 저장소에서 다시 확인합니다.

```properties
autoPublish=true
refreshSnapshots=true
```

비활성화하려면 `false`로 변경하세요.

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
./gradlew :core-data:build
./gradlew :core-logging:build
./gradlew :core-web:build

# 특정 모듈만 빌드
./core-security/gradlew :core-security:build
./gradlew :core-security-oauth2:build
./gradlew :core-data:build
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
