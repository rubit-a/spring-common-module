package rubit.coresecurity.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

@DisplayName("AuthProperties 테스트")
class AuthPropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AuthAutoConfiguration::class.java))

    @Test
    @DisplayName("auth.mode가 없으면 기본값은 JWT다")
    fun defaultAuthModeIsJwt() {
        contextRunner
            .run { context ->
                val properties = context.getBean(AuthProperties::class.java)
                assertEquals(AuthMode.JWT, properties.mode)
            }
    }

    @Test
    @DisplayName("auth.mode=session이면 SESSION으로 바인딩된다")
    fun authModeBindsToSession() {
        contextRunner
            .withPropertyValues("auth.mode=session")
            .run { context ->
                val properties = context.getBean(AuthProperties::class.java)
                assertEquals(AuthMode.SESSION, properties.mode)
            }
    }

    @Test
    @DisplayName("auth.mode가 유효하지 않으면 바인딩이 실패한다")
    fun invalidAuthModeFailsBinding() {
        contextRunner
            .withPropertyValues("auth.mode=invalid")
            .run { context ->
                assertNotNull(context.startupFailure)
            }
    }
}
