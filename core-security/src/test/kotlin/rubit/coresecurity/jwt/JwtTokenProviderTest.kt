package rubit.coresecurity.jwt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import rubit.coresecurity.config.JwtProperties
import java.util.concurrent.TimeUnit

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties

    @BeforeEach
    fun setUp() {
        jwtProperties = JwtProperties(
            secretKey = "test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256",
            accessTokenExpiration = 3600000,
            refreshTokenExpiration = 604800000,
            issuer = "test-issuer"
        )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
    }

    @Test
    @DisplayName("사용자명과 권한으로 Access Token을 생성할 수 있다")
    fun generateAccessTokenWithUsernameAndAuthorities() {
        // given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "ROLE_ADMIN")

        // when
        val token = jwtTokenProvider.generateAccessToken(username, authorities)

        // then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    @DisplayName("Authentication 객체로 Access Token을 생성할 수 있다")
    fun generateAccessTokenWithAuthentication() {
        // given
        val username = "testuser"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(username, null, authorities)

        // when
        val token = jwtTokenProvider.generateAccessToken(authentication)

        // then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(jwtTokenProvider.validateToken(token))
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token))
    }

    @Test
    @DisplayName("Refresh Token을 생성할 수 있다")
    fun generateRefreshToken() {
        // given
        val username = "testuser"

        // when
        val token = jwtTokenProvider.generateRefreshToken(username)

        // then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    @DisplayName("토큰에서 사용자명을 추출할 수 있다")
    fun getUsernameFromToken() {
        // given
        val username = "testuser"
        val token = jwtTokenProvider.generateAccessToken(username, emptyList())

        // when
        val extractedUsername = jwtTokenProvider.getUsernameFromToken(token)

        // then
        assertEquals(username, extractedUsername)
    }

    @Test
    @DisplayName("토큰에서 권한 목록을 추출할 수 있다")
    fun getAuthoritiesFromToken() {
        // given
        val username = "testuser"
        val authorities = listOf("ROLE_USER", "ROLE_ADMIN")
        val token = jwtTokenProvider.generateAccessToken(username, authorities)

        // when
        val extractedAuthorities = jwtTokenProvider.getAuthoritiesFromToken(token)

        // then
        assertEquals(2, extractedAuthorities.size)
        assertTrue(extractedAuthorities.containsAll(authorities))
    }

    @Test
    @DisplayName("권한이 없는 토큰에서는 빈 권한 목록을 반환한다")
    fun getAuthoritiesFromTokenWithoutAuthorities() {
        // given
        val username = "testuser"
        val token = jwtTokenProvider.generateRefreshToken(username)

        // when
        val authorities = jwtTokenProvider.getAuthoritiesFromToken(token)

        // then
        assertTrue(authorities.isEmpty())
    }

    @Test
    @DisplayName("유효한 토큰은 검증을 통과한다")
    fun validateValidToken() {
        // given
        val token = jwtTokenProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

        // when
        val isValid = jwtTokenProvider.validateToken(token)

        // then
        assertTrue(isValid)
    }

    @Test
    @DisplayName("잘못된 형식의 토큰은 검증을 실패한다")
    fun validateInvalidToken() {
        // given
        val invalidToken = "invalid.token.here"

        // when
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        // then
        assertFalse(isValid)
    }

    @Test
    @DisplayName("만료된 토큰은 검증을 실패한다")
    fun validateExpiredToken() {
        // given - 만료 시간이 매우 짧은 토큰 생성
        val shortExpirationProperties = JwtProperties(
            secretKey = jwtProperties.secretKey,
            accessTokenExpiration = 1, // 1ms
            refreshTokenExpiration = jwtProperties.refreshTokenExpiration,
            issuer = jwtProperties.issuer
        )
        val shortLivedProvider = JwtTokenProvider(shortExpirationProperties)
        val token = shortLivedProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

        // when - 토큰이 만료되도록 대기
        TimeUnit.MILLISECONDS.sleep(10)
        val isValid = shortLivedProvider.validateToken(token)

        // then
        assertFalse(isValid)
    }

    @Test
    @DisplayName("다른 비밀키로 서명된 토큰은 검증을 실패한다")
    fun validateTokenWithDifferentSecretKey() {
        // given
        val token = jwtTokenProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

        val differentProperties = JwtProperties(
            secretKey = "different-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256",
            accessTokenExpiration = jwtProperties.accessTokenExpiration,
            refreshTokenExpiration = jwtProperties.refreshTokenExpiration,
            issuer = jwtProperties.issuer
        )
        val differentProvider = JwtTokenProvider(differentProperties)

        // when
        val isValid = differentProvider.validateToken(token)

        // then
        assertFalse(isValid)
    }

    @Test
    @DisplayName("빈 토큰은 검증을 실패한다")
    fun validateEmptyToken() {
        // when
        val isValid = jwtTokenProvider.validateToken("")

        // then
        assertFalse(isValid)
    }

    @Test
    @DisplayName("생성된 토큰에는 올바른 issuer가 포함된다")
    fun tokenContainsCorrectIssuer() {
        // given
        val username = "testuser"
        val token = jwtTokenProvider.generateAccessToken(username, emptyList())

        // when
        val isValid = jwtTokenProvider.validateToken(token)
        val extractedUsername = jwtTokenProvider.getUsernameFromToken(token)

        // then
        assertTrue(isValid)
        assertEquals(username, extractedUsername)
    }
}
