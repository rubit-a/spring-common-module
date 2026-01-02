# Test Web - 공통 모듈 테스트 프로젝트

이 프로젝트는 여러 공통 모듈을 통합하여 테스트하는 웹 애플리케이션입니다.

## 프로젝트 구조

```
test-web/
├── src/main/kotlin/rubit/testweb/
│   ├── TestWebApplication.kt          # Spring Boot 메인 애플리케이션
│   │
│   └── commonauth/                    # common-auth 모듈 관련 코드
│       ├── config/
│       │   └── SecurityConfig.kt      # Spring Security 설정
│       ├── controller/
│       │   ├── AuthController.kt      # 인증 관련 API (로그인, 토큰 검증)
│       │   ├── PublicController.kt    # 공개 API (인증 불필요)
│       │   └── UserController.kt      # 보호된 API (인증 필요)
│       └── dto/
│           └── AuthDto.kt             # 요청/응답 DTO
│
└── src/main/resources/
    └── application.yml                # 애플리케이션 설정
```

### 모듈별 디렉토리 구조

각 공통 모듈에 대한 테스트 코드는 해당 모듈 이름의 패키지 하위에 위치합니다:

- `commonauth/` - `common-auth` 모듈 관련 코드
- `(향후 추가될 모듈)/` - 예: `commonlog/`, `commondb/` 등

이러한 구조를 통해 여러 공통 모듈을 동시에 테스트하면서도 각 모듈의 기능을 명확하게 구분할 수 있습니다.

## 현재 통합된 모듈

### 1. common-auth (JWT 인증)

**주요 기능:**
- JWT Access Token 및 Refresh Token 발급
- 토큰 기반 인증 및 권한 관리
- Spring Security 통합

**관련 파일:**
- `commonauth/config/SecurityConfig.kt` - Security 설정
- `commonauth/controller/AuthController.kt` - 로그인 API
- `commonauth/controller/UserController.kt` - 인증이 필요한 API
- `commonauth/controller/PublicController.kt` - 공개 API

## API 엔드포인트

### common-auth 모듈 테스트 API

#### 1. 인증이 필요 없는 엔드포인트 (공개)

- `POST /api/auth/login` - 로그인 (JWT 토큰 발급)
- `GET /api/auth/validate` - 토큰 유효성 검증
- `GET /api/auth/user-info` - 토큰에서 사용자 정보 추출
- `GET /api/public/hello` - 공개 엔드포인트
- `GET /api/public/health` - 헬스 체크

#### 2. 인증이 필요한 엔드포인트 (보호)

- `GET /api/users/me` - 현재 로그인한 사용자 정보
- `GET /api/users/profile` - 사용자 프로필
- `GET /api/users/admin` - 관리자 전용 (ROLE_ADMIN 필요)

## 실행 방법

### 1. 프로젝트 빌드

루트 디렉토리에서:
```bash
./gradlew :test-web:build
```

또는 test-web 디렉토리에서:
```bash
cd test-web
../gradlew build
```

### 2. 애플리케이션 실행

```bash
./gradlew :test-web:bootRun
```

또는 JAR 파일 직접 실행:
```bash
java -jar test-web/build/libs/test-web-0.0.1-SNAPSHOT.jar
```

애플리케이션이 `http://localhost:8080`에서 실행됩니다.

## API 사용 예제

### 1. 로그인 (토큰 발급)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

응답:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### 2. 공개 엔드포인트 호출 (인증 불필요)

```bash
curl http://localhost:8080/api/public/hello
```

응답:
```json
{
  "message": "Hello! This is a public endpoint.",
  "access": "No authentication required"
}
```

### 3. 보호된 엔드포인트 호출 (인증 필요)

먼저 토큰을 환경 변수에 저장:
```bash
TOKEN="여기에_로그인에서_받은_accessToken을_붙여넣기"
```

토큰과 함께 요청:
```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

응답:
```json
{
  "username": "testuser",
  "authorities": ["ROLE_USER"],
  "authenticated": true
}
```

### 4. 관리자 전용 엔드포인트 호출

관리자 권한으로 로그인:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

관리자 토큰으로 요청:
```bash
ADMIN_TOKEN="여기에_admin_로그인에서_받은_accessToken을_붙여넣기"

curl http://localhost:8080/api/users/admin \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

응답:
```json
{
  "message": "Welcome admin: admin",
  "level": "ADMIN_ONLY"
}
```

일반 사용자 토큰으로 요청하면 403 Forbidden 에러 발생.

## common-auth 모듈 구현 예제

### SecurityConfig.kt

`common-auth`의 `JwtAuthenticationFilter`를 사용하여 JWT 인증을 자동으로 처리합니다.

```kotlin
package rubit.testweb.commonauth.config

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {

    @Bean
    fun jwtSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

### AuthController.kt

`common-auth`의 `JwtTokenProvider`를 주입받아 토큰을 생성합니다.

```kotlin
package rubit.testweb.commonauth.controller

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(
            username = request.username,
            authorities = listOf("ROLE_USER")
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(
            username = request.username
        )
        return TokenResponse(accessToken, refreshToken)
    }
}
```

### UserController.kt

`@PreAuthorize` 어노테이션을 사용하여 역할 기반 접근 제어를 구현합니다.

```kotlin
package rubit.testweb.commonauth.controller

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun adminOnly(authentication: Authentication): Map<String, String> {
        return mapOf(
            "message" to "Welcome admin: ${authentication.name}",
            "level" to "ADMIN_ONLY"
        )
    }
}
```

## 테스트 계정

로그인 API는 데모 목적으로 간단한 인증 로직을 사용합니다:

- **일반 사용자**: username 길이가 3자 이상인 모든 사용자
  - 권한: `ROLE_USER`

- **관리자**: username이 "admin"인 경우
  - 권한: `ROLE_ADMIN`, `ROLE_USER`

실제 프로덕션 환경에서는 데이터베이스와 비밀번호 암호화를 사용해야 합니다.

## 설정

### application.yml

```yaml
spring:
  application:
    name: test-web

server:
  port: 8080

# common-auth 모듈 설정
jwt:
  secret-key: my-super-secret-key-for-jwt-token-signing-must-be-at-least-256-bits-long
  access-token-expiration: 3600000  # 1시간
  refresh-token-expiration: 604800000  # 7일
  issuer: test-web
```

## 의존성

```kotlin
dependencies {
    // 공통 모듈
    implementation(project(":common-auth"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
}
```

## 새로운 공통 모듈 추가하기

새로운 공통 모듈을 테스트하려면:

1. **의존성 추가** (`build.gradle.kts`):
```kotlin
dependencies {
    implementation(project(":common-auth"))
    implementation(project(":새로운-모듈"))  // 추가
}
```

2. **모듈별 패키지 생성**:
```
src/main/kotlin/rubit/testweb/
└── 새로운모듈/           # 새 모듈 관련 코드
    ├── config/
    ├── controller/
    └── dto/
```

3. **설정 추가** (`application.yml`):
```yaml
# 새로운 모듈 설정
새로운모듈:
  property: value
```

이러한 구조를 통해 여러 공통 모듈을 독립적으로 관리하면서도 통합 테스트를 수행할 수 있습니다.

## 참고

- [common-auth 모듈 문서](../common-auth/README.md)
- [프로젝트 루트 문서](../README.md)
