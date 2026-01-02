package rubit.corelogging.aop

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.aop.support.AopUtils
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import rubit.corelogging.config.CoreLoggingAutoConfiguration

@DisplayName("LogExecutionTimeAspect 테스트")
class LogExecutionTimeAspectTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                AopAutoConfiguration::class.java,
                CoreLoggingAutoConfiguration::class.java
            )
        )
        .withUserConfiguration(TestConfig::class.java)

    @Test
    @DisplayName("aop.enabled=true이면 Aspect 빈이 등록되고 프록시가 적용된다")
    fun `registers aspect and proxies annotated beans when enabled`() {
        contextRunner
            .withPropertyValues("core.logging.aop.enabled=true")
            .run { context ->
                val service = context.getBean(SampleService::class.java)

                assertNotNull(context.getBean(LogExecutionTimeAspect::class.java))
                assertTrue(AopUtils.isAopProxy(service))
                assertEquals("ok", service.work())
            }
    }

    @Test
    @DisplayName("aop.enabled=false이면 Aspect 빈이 등록되지 않는다")
    fun `does not register aspect when disabled`() {
        contextRunner
            .run { context ->
                val service = context.getBean(SampleService::class.java)

                assertFalse(context.containsBean("logExecutionTimeAspect"))
                assertFalse(AopUtils.isAopProxy(service))
            }
    }

    @Configuration(proxyBeanMethods = false)
    class TestConfig {
        @Bean
        fun sampleService(): SampleService = SampleService()
    }

    open class SampleService {
        @LogExecutionTime
        open fun work(): String = "ok"
    }
}
