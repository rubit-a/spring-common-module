package rubit.commonauth.filter

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import rubit.commonauth.config.JwtProperties
import rubit.commonauth.jwt.JwtTokenProvider

@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        val jwtProperties = JwtProperties(
            secretKey = "test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256",
            accessTokenExpiration = 3600000,
            refreshTokenExpiration = 604800000,
            issuer = "test-issuer"
        )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider)

        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        filterChain = mock()

        // SecurityContext 초기화
        SecurityContextHolder.clearContext()
    }

    @Test
    @DisplayName("유효한 JWT 토큰이 있으면 SecurityContext에 인증 정보를 설정한다")
    fun doFilterWithValidToken() {
        // given
        val username = "testuser"
        val authorities = listOf("ROLE_USER")
        val token = jwtTokenProvider.generateAccessToken(username, authorities)

        request.addHeader("Authorization", "Bearer $token")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals(username, authentication?.name)
        assertTrue(authentication?.authorities?.any { it.authority == "ROLE_USER" } ?: false)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보를 설정하지 않는다")
    fun doFilterWithoutAuthorizationHeader() {
        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("Bearer 접두사가 없으면 인증 정보를 설정하지 않는다")
    fun doFilterWithoutBearerPrefix() {
        // given
        val token = jwtTokenProvider.generateAccessToken("testuser", listOf("ROLE_USER"))
        request.addHeader("Authorization", token) // Bearer 없이

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("잘못된 토큰이면 인증 정보를 설정하지 않는다")
    fun doFilterWithInvalidToken() {
        // given
        request.addHeader("Authorization", "Bearer invalid.token.here")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("만료된 토큰이면 인증 정보를 설정하지 않는다")
    fun doFilterWithExpiredToken() {
        // given - 만료된 토큰 생성
        val shortExpirationProperties = JwtProperties(
            secretKey = "test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256",
            accessTokenExpiration = 1,
            refreshTokenExpiration = 604800000,
            issuer = "test-issuer"
        )
        val shortLivedProvider = JwtTokenProvider(shortExpirationProperties)
        val token = shortLivedProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

        Thread.sleep(10) // 토큰 만료 대기

        request.addHeader("Authorization", "Bearer $token")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("여러 권한을 가진 토큰을 올바르게 처리한다")
    fun doFilterWithMultipleAuthorities() {
        // given
        val username = "admin"
        val authorities = listOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
        val token = jwtTokenProvider.generateAccessToken(username, authorities)

        request.addHeader("Authorization", "Bearer $token")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals(username, authentication?.name)
        assertEquals(3, authentication?.authorities?.size)
        assertTrue(authentication?.authorities?.any { it.authority == "ROLE_USER" } ?: false)
        assertTrue(authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } ?: false)
        assertTrue(authentication?.authorities?.any { it.authority == "ROLE_MANAGER" } ?: false)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("권한이 없는 토큰도 올바르게 처리한다")
    fun doFilterWithNoAuthorities() {
        // given
        val username = "testuser"
        val token = jwtTokenProvider.generateAccessToken(username, emptyList())

        request.addHeader("Authorization", "Bearer $token")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals(username, authentication?.name)
        assertTrue(authentication?.authorities?.isEmpty() ?: false)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("빈 Bearer 토큰은 인증 정보를 설정하지 않는다")
    fun doFilterWithEmptyBearerToken() {
        // given
        request.addHeader("Authorization", "Bearer ")

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    @DisplayName("필터 체인은 항상 실행된다")
    fun filterChainAlwaysExecutes() {
        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        // then
        verify(filterChain, times(1)).doFilter(request, response)
    }
}
