package rubit.coresecurity.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import rubit.coresecurity.filter.JwtAuthenticationFilter
import rubit.coresecurity.jwt.JwtTokenProvider

@DisplayName("JwtAutoConfiguration 테스트")
class JwtAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JwtAutoConfiguration::class.java))

    @Test
    @DisplayName("secret-key가 있으면 JwtTokenProvider 빈이 등록된다")
    fun jwtTokenProviderBeanIsRegisteredWithSecretKey() {
        contextRunner
            .withPropertyValues(
                "jwt.secret-key=test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256",
                "jwt.access-token-expiration=3600000",
                "jwt.refresh-token-expiration=604800000",
                "jwt.issuer=test-issuer"
            )
            .run { context ->
                assertNotNull(context.getBean(JwtTokenProvider::class.java))
            }
    }

    @Test
    @DisplayName("secret-key가 있으면 JwtAuthenticationFilter 빈이 등록된다")
    fun jwtAuthenticationFilterBeanIsRegisteredWithSecretKey() {
        contextRunner
            .withPropertyValues(
                "jwt.secret-key=test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256"
            )
            .run { context ->
                assertNotNull(context.getBean(JwtAuthenticationFilter::class.java))
            }
    }

    @Test
    @DisplayName("등록된 JwtTokenProvider로 토큰을 생성할 수 있다")
    fun canGenerateTokenWithRegisteredProvider() {
        contextRunner
            .withPropertyValues(
                "jwt.secret-key=test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256"
            )
            .run { context ->
                val jwtTokenProvider = context.getBean(JwtTokenProvider::class.java)
                val token = jwtTokenProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

                assertNotNull(token)
                assertTrue(token.isNotBlank())
                assertTrue(jwtTokenProvider.validateToken(token))
            }
    }

    @Test
    @DisplayName("secret-key가 없으면 JwtTokenProvider 빈이 등록되지 않는다")
    fun jwtTokenProviderBeanIsNotRegisteredWithoutSecretKey() {
        contextRunner
            .run { context ->
                assertFalse(context.containsBean("jwtTokenProvider"))
            }
    }

    @Test
    @DisplayName("auth.mode=session이면 JwtTokenProvider 빈이 등록되지 않는다")
    fun jwtTokenProviderBeanIsNotRegisteredWithSessionMode() {
        contextRunner
            .withPropertyValues(
                "auth.mode=session",
                "jwt.secret-key=test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256"
            )
            .run { context ->
                assertFalse(context.containsBean("jwtTokenProvider"))
            }
    }

    @Test
    @DisplayName("SecurityFilterChain 빈이 있으면 jwtSecurityFilterChain 빈이 등록되지 않는다")
    fun jwtSecurityFilterChainIsNotRegisteredWhenCustomChainExists() {
        contextRunner
            .withUserConfiguration(CustomSecurityConfig::class.java)
            .withPropertyValues(
                "jwt.secret-key=test-secret-key-for-jwt-must-be-at-least-256-bits-long-for-hmac-sha256"
            )
            .run { context ->
                assertTrue(context.containsBean("customSecurityFilterChain"))
                assertFalse(context.containsBean("jwtSecurityFilterChain"))
            }
    }

    @Configuration
    class CustomSecurityConfig {
        @Bean
        fun customSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            return http
                .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
                .build()
        }
    }
}
