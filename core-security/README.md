# Core Security - 인증 공통 모듈 (JWT/Session)

다른 Spring Boot 애플리케이션에서 공통으로 사용할 수 있는 인증 라이브러리입니다.

## 기능

- JWT Access Token 생성 및 검증
- JWT Refresh Token 생성
- Spring Security와 통합된 JWT 인증 필터
- Auto Configuration을 통한 자동 설정
- 인증 모드 선택 지원 (`jwt` 또는 `session`)
- `@CurrentUser`로 인증 사용자 주입
- SecurityContext 헬퍼 유틸 제공
- 기본 `PasswordEncoder` 제공 (BCrypt)

## 사용 방법

### 1. 의존성 추가

다른 프로젝트의 `build.gradle.kts`에 다음 의존성을 추가하세요:

```kotlin
dependencies {
    implementation(project(":core-security"))
    // 또는 Maven 저장소에 배포한 경우
    // implementation("rubit:core-security:0.0.1-SNAPSHOT")
}
```

### 2. 설정 추가

`application.yml` 또는 `application.properties`에 설정을 추가하세요:

```yaml
auth:
  mode: jwt
  password-encoder:
    enabled: true
    strength: 10

jwt:
  secret-key: your-secret-key-here-minimum-256-bits-long
  access-token-expiration: 3600000  # 1시간 (밀리초)
  refresh-token-expiration: 604800000  # 7일 (밀리초)
  issuer: your-app-name
```

또는 `application.properties`:

```properties
auth.mode=jwt
auth.password-encoder.enabled=true
auth.password-encoder.strength=10
jwt.secret-key=your-secret-key-here-minimum-256-bits-long
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000
jwt.issuer=your-app-name
```

**중요**: `secret-key`는 최소 256비트(32자) 이상이어야 합니다.

#### Session 모드

JWT 없이 세션 기반으로 동작하려면 다음과 같이 설정하세요:

```yaml
auth:
  mode: session
```

Session 모드는 기본적으로 `SessionCreationPolicy.IF_REQUIRED`를 사용합니다.
기본 체인은 `anyRequest().authenticated()`로 동작하며 CSRF는 기본값(활성화)입니다.
필요한 인증/인가 정책은 애플리케이션에서 `SecurityFilterChain`을 정의해 주세요.
`auth.mode=session`일 때는 JWT 자동 설정이 비활성화됩니다.

#### PasswordEncoder

별도 설정이 없으면 `BCryptPasswordEncoder`가 자동 등록됩니다.

```yaml
auth:
  password-encoder:
    enabled: true
    strength: 10
```

### 3. @CurrentUser 사용

컨트롤러 파라미터에 `@CurrentUser`를 붙이면 인증 사용자 정보를 주입합니다.

```kotlin
@GetMapping("/me")
fun me(@CurrentUser username: String?): Map<String, String?> {
    return mapOf("username" to username)
}
```

지원 타입:

- `String` (username)
- `Authentication`
- `Principal`
- `UserDetails` (principal이 UserDetails인 경우)

### 4. SecurityContextUtils

```kotlin
val username = SecurityContextUtils.getUsername()
val authorities = SecurityContextUtils.getAuthorities()
val isAuthenticated = SecurityContextUtils.isAuthenticated()
```

### 5. JWT 토큰 사용

#### 3.1 토큰 생성

```kotlin
@RestController
@RequestMapping("/auth")
class JwtAuthController(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): TokenResponse {
        // 사용자 인증 로직 (생략)

        val accessToken = jwtTokenProvider.generateAccessToken(
            username = loginRequest.username,
            authorities = listOf("ROLE_USER")
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            username = loginRequest.username
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}
```

#### 3.2 Spring Security Authentication 사용

```kotlin
@PostMapping("/login-with-auth")
fun loginWithAuth(authentication: Authentication): TokenResponse {
    val accessToken = jwtTokenProvider.generateAccessToken(authentication)
    val refreshToken = jwtTokenProvider.generateRefreshToken(authentication.name)

    return TokenResponse(
        accessToken = accessToken,
        refreshToken = refreshToken
    )
}
```

#### 3.3 토큰 검증

```kotlin
@GetMapping("/validate")
fun validateToken(@RequestParam token: String): Boolean {
    return jwtTokenProvider.validateToken(token)
}
```

#### 3.4 토큰에서 정보 추출

```kotlin
@GetMapping("/user-info")
fun getUserInfo(@RequestParam token: String): UserInfo {
    val username = jwtTokenProvider.getUsernameFromToken(token)
    val authorities = jwtTokenProvider.getAuthoritiesFromToken(token)

    return UserInfo(username = username, authorities = authorities)
}
```

### 4. 보호된 엔드포인트 설정

기본 설정은 모든 요청을 허용합니다. 보호가 필요한 엔드포인트가 있다면 직접 `SecurityFilterChain`을 정의하세요.
애플리케이션에서 `SecurityFilterChain`을 정의하면 core-security의 기본 체인은 등록되지 않습니다.

```kotlin
@Configuration
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
                    .requestMatchers("/auth/**", "/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

### 5. API 호출 시 토큰 사용

클라이언트에서 API를 호출할 때는 Authorization 헤더에 토큰을 포함하세요:

```
Authorization: Bearer {your-jwt-token}
```

예시 (curl):
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
     http://localhost:8080/api/protected-resource
```

## 구성 요소

### Auth Mode
인증 방식 선택을 위한 설정입니다.

설정 가능한 속성:
- `auth.mode`: 인증 모드 (`jwt` 또는 `session`, 기본값: `jwt`)

### JwtTokenProvider
JWT 토큰 생성 및 검증을 담당하는 핵심 클래스입니다.

주요 메서드:
- `generateAccessToken(username, authorities)`: Access Token 생성
- `generateRefreshToken(username)`: Refresh Token 생성
- `validateToken(token)`: 토큰 유효성 검증
- `getUsernameFromToken(token)`: 토큰에서 사용자명 추출
- `getAuthoritiesFromToken(token)`: 토큰에서 권한 목록 추출

### JwtAuthenticationFilter
HTTP 요청에서 JWT 토큰을 추출하고 인증을 처리하는 필터입니다.
Authorization 헤더에서 "Bearer " 접두사를 가진 토큰을 자동으로 추출하여 검증합니다.

### JwtProperties
JWT 관련 설정을 관리하는 클래스입니다.

설정 가능한 속성:
- `jwt.secret-key`: JWT 서명에 사용할 비밀 키 (필수)
- `jwt.access-token-expiration`: Access Token 만료 시간 (기본값: 1시간)
- `jwt.refresh-token-expiration`: Refresh Token 만료 시간 (기본값: 7일)
- `jwt.issuer`: 토큰 발급자 (기본값: "core-security")

## 빌드

```bash
./gradlew build
```

## 라이선스

이 프로젝트는 비공개 프로젝트입니다.
