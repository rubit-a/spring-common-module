package rubit.coresecurity.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JwtProperties 테스트")
class JwtPropertiesTest {

    @Test
    @DisplayName("JwtProperties를 생성할 수 있다")
    fun createJwtProperties() {
        // given & when
        val jwtProperties = JwtProperties(
            secretKey = "test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256",
            accessTokenExpiration = 7200000,
            refreshTokenExpiration = 1209600000,
            issuer = "custom-issuer"
        )

        // then
        assertNotNull(jwtProperties)
        assertEquals("test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256", jwtProperties.secretKey)
        assertEquals(7200000, jwtProperties.accessTokenExpiration)
        assertEquals(1209600000, jwtProperties.refreshTokenExpiration)
        assertEquals("custom-issuer", jwtProperties.issuer)
    }

    @Test
    @DisplayName("기본값을 사용하여 JwtProperties를 생성할 수 있다")
    fun createJwtPropertiesWithDefaultValues() {
        // given & when
        val jwtProperties = JwtProperties(
            secretKey = "test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256"
        )

        // then
        assertNotNull(jwtProperties)
        assertEquals("test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256", jwtProperties.secretKey)
        assertEquals(3600000, jwtProperties.accessTokenExpiration) // 기본값 1시간
        assertEquals(604800000, jwtProperties.refreshTokenExpiration) // 기본값 7일
        assertEquals("core-security", jwtProperties.issuer) // 기본값
    }

    @Test
    @DisplayName("각 속성을 독립적으로 설정할 수 있다")
    fun setPropertiesIndependently() {
        // given & when
        val jwtProperties = JwtProperties(
            secretKey = "my-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algorithm",
            accessTokenExpiration = 1800000, // 30분
            refreshTokenExpiration = 2592000000, // 30일
            issuer = "my-app"
        )

        // then
        assertEquals("my-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256-algorithm", jwtProperties.secretKey)
        assertEquals(1800000, jwtProperties.accessTokenExpiration)
        assertEquals(2592000000, jwtProperties.refreshTokenExpiration)
        assertEquals("my-app", jwtProperties.issuer)
    }
}
