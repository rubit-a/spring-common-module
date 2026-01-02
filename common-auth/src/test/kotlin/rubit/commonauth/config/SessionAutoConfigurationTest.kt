package rubit.commonauth.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

@DisplayName("SessionAutoConfiguration 테스트")
class SessionAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SessionAutoConfiguration::class.java))

    @Test
    @DisplayName("auth.mode=session이면 sessionSecurityFilterChain 빈이 등록된다")
    fun sessionSecurityFilterChainBeanIsRegisteredWithSessionMode() {
        contextRunner
            .withPropertyValues("auth.mode=session")
            .run { context ->
                assertTrue(context.containsBean("sessionSecurityFilterChain"))
            }
    }

    @Test
    @DisplayName("auth.mode가 없으면 sessionSecurityFilterChain 빈이 등록되지 않는다")
    fun sessionSecurityFilterChainBeanIsNotRegisteredWithoutMode() {
        contextRunner
            .run { context ->
                assertFalse(context.containsBean("sessionSecurityFilterChain"))
            }
    }
}
