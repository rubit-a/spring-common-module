# Core Security OAuth2 - OAuth2 로그인 + 자체 JWT 발급

Spring Security OAuth2 로그인 후 자체 JWT를 발급하는 공통 모듈입니다.
`core-security`의 JWT 토큰 발급기를 사용하며, 기본적으로 세션 없이 동작합니다.

## 기능

- OAuth2 로그인 (Authorization Code)
- JWT Access/Refresh 토큰 발급
- 쿠키 기반 Authorization Request 저장 (세션 미사용)
- Auto Configuration 지원

## 사용 방법

### 1. 의존성 추가

```kotlin
dependencies {
    implementation(project(":core-security-oauth2"))
    // 또는 Maven 저장소에 배포한 경우
    // implementation("rubit:core-security-oauth2:0.0.1-SNAPSHOT")
}
```

### 2. OAuth2 공급자 설정

`application.yml` 또는 `application.properties`에 OAuth2 Client 설정을 추가하세요.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
            scope: profile, email
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://openidconnect.googleapis.com/v1/userinfo
            user-name-attribute: sub
```

### 3. 공통 설정

```yaml
auth:
  mode: jwt
  oauth2:
    enabled: true
    response-mode: JSON     # JSON or REDIRECT
    success-redirect-uri: https://your-frontend.example.com/oauth2/callback
    authorized-redirect-uris: https://your-frontend.example.com
    redirect-param-name: redirect_uri
    principal-attribute: email
    default-authorities: ROLE_USER
    cookie-expire-seconds: 180

jwt:
  secret-key: your-secret-key-here-minimum-256-bits-long
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000
  issuer: your-app-name
```

- `response-mode=JSON`: 성공 시 JWT를 JSON으로 응답합니다.
- `response-mode=REDIRECT`: `success-redirect-uri`로 리다이렉트하며, 쿼리 파라미터로 토큰을 전달합니다.
- `authorized-redirect-uris`: `redirect_uri` 파라미터를 허용할 도메인 목록입니다.

## 기본 엔드포인트

- 로그인 시작: `/oauth2/authorization/{registrationId}`
- 콜백: `/login/oauth2/code/{registrationId}`

추가로 `redirect_uri` 파라미터를 전달하면 해당 URI로 리다이렉트합니다.

예시:
```
/oauth2/authorization/google?redirect_uri=https://your-frontend.example.com/oauth2/callback
```

## 인증 흐름 (엔드포인트 기준)

1. 사용자가 프론트에서 로그인 클릭 → 백엔드로 이동
   - `GET https://{backend}/oauth2/authorization/google?redirect_uri=https://{frontend}/oauth2/callback`
2. 백엔드가 Google 인증 페이지로 리다이렉트
   - `302 https://accounts.google.com/o/oauth2/v2/auth?...`
3. 사용자가 Google 로그인/동의
4. Google이 백엔드 콜백으로 리다이렉트
   - `GET https://{backend}/login/oauth2/code/google?code=...&state=...`
5. 백엔드가 code 교환 → 사용자 정보 조회 → (선택) 사용자 매핑 → 자체 JWT 발급
6. 성공 응답
   - `response-mode=REDIRECT`일 때
     - `302 https://{frontend}/oauth2/callback?access_token=...&refresh_token=...`
   - `response-mode=JSON`일 때
     - 콜백에서 JSON으로 토큰 응답 (팝업/새창 처리 필요)
7. 프론트는 토큰 저장 후 다음 페이지로 라우팅

> 참고: `redirect_uri`는 `authorized-redirect-uris`에 등록된 도메인만 허용됩니다.

## 토큰 응답 형식 (JSON)

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## 보안 설정 커스터마이징

기본 설정을 사용하지 않고 직접 `SecurityFilterChain`을 정의할 수도 있습니다.
아래는 최소 예시입니다:

```kotlin
@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authorizationRequestRepository: AuthorizationRequestRepository<OAuth2AuthorizationRequest>,
        successHandler: AuthenticationSuccessHandler,
        failureHandler: AuthenticationFailureHandler,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(authorizationRequestRepository)
                }
                oauth2.successHandler(successHandler)
                oauth2.failureHandler(failureHandler)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
```

## 빌드

```bash
./gradlew :core-security-oauth2:build
```

## 라이선스

이 프로젝트는 비공개 프로젝트입니다.
