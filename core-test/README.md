# Core Test - 공통 모듈 테스트 프로젝트

이 프로젝트는 여러 공통 모듈을 통합하여 테스트하는 웹 애플리케이션입니다.

## 프로젝트 구조

```
core-test/
├── src/main/kotlin/rubit/coretest/
│   ├── CoreTestApplication.kt          # Spring Boot 메인 애플리케이션
│   │
│   └── coreauth/                    # core-security 모듈 관련 코드
│       ├── config/
│       │   └── SecurityConfig.kt      # Spring Security 설정 (JWT/Session)
│       ├── controller/
│       │   ├── JwtAuthController.kt   # JWT 인증 관련 API (로그인, 토큰 검증)
│       │   ├── SessionAuthController.kt # 세션 로그인 API
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

- `coreauth/` - `core-security` 모듈 관련 코드
- `(향후 추가될 모듈)/` - 예: `commonlog/`, `commondb/` 등

이러한 구조를 통해 여러 공통 모듈을 동시에 테스트하면서도 각 모듈의 기능을 명확하게 구분할 수 있습니다.

## 현재 통합된 모듈

### 1. core-security (JWT/Session 인증)

**주요 기능:**
- JWT Access Token 및 Refresh Token 발급
- 토큰 기반 인증 및 권한 관리
- Spring Security 통합
- 세션 기반 인증 모드 지원

**관련 파일:**
- `coreauth/config/SecurityConfig.kt` - Security 설정
- `coreauth/controller/JwtAuthController.kt` - JWT 로그인 API
- `coreauth/controller/UserController.kt` - 인증이 필요한 API
- `coreauth/controller/PublicController.kt` - 공개 API

### 2. core-data (공통 JPA 인프라)

**주요 기능:**
- BaseEntity/BaseAuditEntity 공통 컬럼 제공
- JPA Auditing 자동 활성화

**관련 파일:**
- `coredata/entity/SampleEntity.kt` - BaseAuditEntity 사용 예제
- `coredata/controller/SampleController.kt` - 샘플 CRUD API

### 3. core-excel (엑셀 생성/다운로드)

**주요 기능:**
- 템플릿/데이터 공급자 기반 엑셀 생성
- 다운로드 엔드포인트 제공

**관련 파일:**
- `coreexcel/UserExcelTemplate.kt` - 엑셀 템플릿 정의
- `coreexcel/UserExcelDataProvider.kt` - 데이터 공급자

## API 엔드포인트

### core-security 모듈 테스트 API

#### 1. 인증이 필요 없는 엔드포인트 (공개)

- `POST /api/auth/login` - 로그인 (JWT 토큰 발급)
- `GET /api/auth/validate` - 토큰 유효성 검증
- `GET /api/auth/user-info` - 토큰에서 사용자 정보 추출
- `GET /api/public/hello` - 공개 엔드포인트
- `GET /api/public/health` - 헬스 체크

#### 1-1. Session 모드 전용 엔드포인트

- `POST /api/session/login` - 로그인 (세션 생성)
- `POST /api/session/logout` - 로그아웃 (세션 삭제)

#### 2. 인증이 필요한 엔드포인트 (보호)

- `GET /api/users/me` - 현재 로그인한 사용자 정보
- `GET /api/users/profile` - 사용자 프로필
- `GET /api/users/admin` - 관리자 전용 (ROLE_ADMIN 필요)

### core-data 모듈 테스트 API (인증 필요)

- `POST /api/data/samples` - 샘플 생성
- `GET /api/data/samples` - 샘플 목록
- `PATCH /api/data/samples/{id}` - 샘플 수정
- `GET /api/data/settings` - JPA 설정 확인

### core-excel 모듈 테스트 API (인증 필요)

- `GET /api/excel/users` - 사용자 목록 엑셀 다운로드
- `GET /api/excel/users?enabled=true` - 활성 사용자 엑셀 다운로드

## 실행 방법

### 1. 프로젝트 빌드

루트 디렉토리에서:
```bash
./gradlew :core-test:build
```

또는 core-test 디렉토리에서:
```bash
cd core-test
../gradlew build
```

### 2. 애플리케이션 실행

```bash
./gradlew :core-test:bootRun
```

또는 JAR 파일 직접 실행:
```bash
java -jar core-test/build/libs/core-test-0.0.1-SNAPSHOT.jar
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

### 5. Session 모드 로그인/요청

`application.yml`에서 `auth.mode: session`으로 변경한 뒤 사용하세요.

로그인 (세션 쿠키 저장):
```bash
curl -X POST http://localhost:8080/api/session/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

세션 쿠키로 보호된 엔드포인트 호출:
```bash
curl http://localhost:8080/api/users/me \
  -b cookies.txt
```

로그아웃:
```bash
curl -X POST http://localhost:8080/api/session/logout \
  -b cookies.txt
```

## core-security 모듈 구현 예제

### SecurityConfig.kt

JWT 모드에서는 `JwtAuthenticationFilter`로 토큰 인증을 처리합니다.

```kotlin
package rubit.coretest.coreauth.config

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {

    @Bean
    @ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "jwt", matchIfMissing = true)
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

### SessionAuthController.kt

세션 기반 로그인 시에는 인증을 수행하고 세션을 생성합니다.
세션 사용자 조회는 JPA 기반 `UserDetailsService`를 사용합니다.
테스트 계정은 애플리케이션 시작 시 JPA로 저장됩니다.

```kotlin
package rubit.coretest.coreauth.controller

@RestController
@RequestMapping("/api/session")
class SessionAuthController(
    private val authenticationManager: AuthenticationManager
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest, httpRequest: HttpServletRequest): UserInfoResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        httpRequest.session

        return UserInfoResponse(
            username = authentication.name,
            authorities = authentication.authorities.mapNotNull { it.authority }
        )
    }
}
```

### JwtAuthController.kt

`AuthenticationManager`로 인증한 뒤 `JwtTokenProvider`로 토큰을 생성합니다.

```kotlin
package rubit.coretest.coreauth.controller

@RestController
@RequestMapping("/api/auth")
class JwtAuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(authentication.name)
        return TokenResponse(accessToken, refreshToken)
    }
}
```

### UserController.kt

`@PreAuthorize` 어노테이션을 사용하여 역할 기반 접근 제어를 구현합니다.

```kotlin
package rubit.coretest.coreauth.controller

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

모드별로 인증 방식이 다릅니다:

- **JWT 모드**: 앱 시작 시 JPA로 계정 초기화
  - `testuser/password123` -> `ROLE_USER`
  - `admin/admin123` -> `ROLE_ADMIN`, `ROLE_USER`
- **Session 모드**: 앱 시작 시 JPA로 계정 초기화
  - `testuser/password123` -> `ROLE_USER`
  - `admin/admin123` -> `ROLE_ADMIN`, `ROLE_USER`

실제 프로덕션 환경에서는 사용자 테이블 설계와 비밀번호 암호화를 사용해야 합니다.

## 설정

### application.yml

```yaml
spring:
  application:
    name: core-test
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8080

# 인증 모드 선택 (jwt 또는 session)
auth:
  mode: jwt

# core-security 모듈 설정
jwt:
  secret-key: my-super-secret-key-for-jwt-token-signing-must-be-at-least-256-bits-long
  access-token-expiration: 3600000  # 1시간
  refresh-token-expiration: 604800000  # 7일
  issuer: core-test
```

## 의존성

```kotlin
dependencies {
    // 공통 모듈 (GitHub Packages)
    implementation("rubit:core-security:0.0.1-SNAPSHOT")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    runtimeOnly("com.h2database:h2")
}
```

로컬 모듈을 직접 참조하려면 `-PuseLocalModules=true` 옵션으로 실행하세요.

## 새로운 공통 모듈 추가하기

새로운 공통 모듈을 테스트하려면:

1. **의존성 추가** (`build.gradle.kts`):
```kotlin
dependencies {
    implementation("rubit:core-security:0.0.1-SNAPSHOT")
    implementation("rubit:새로운-모듈:0.0.1-SNAPSHOT")  // 추가
}
```

2. **모듈별 패키지 생성**:
```
src/main/kotlin/rubit/coretest/
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

- [core-security 모듈 문서](../core-security/README.md)
- [프로젝트 루트 문서](../README.md)
