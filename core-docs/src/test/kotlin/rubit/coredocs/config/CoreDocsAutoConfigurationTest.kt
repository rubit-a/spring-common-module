package rubit.coredocs.config

import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("CoreDocsAutoConfiguration 테스트")
class CoreDocsAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CoreDocsAutoConfiguration::class.java))

    @Test
    @DisplayName("core.docs.enabled=true이면 OpenAPI 빈이 등록된다")
    fun openApiBeanRegistered() {
        contextRunner
            .run { context ->
                assertTrue(context.containsBean("openApi"))
                assertNotNull(context.getBean(OpenAPI::class.java))
            }
    }

    @Test
    @DisplayName("설정값이 OpenAPI에 반영된다")
    fun openApiPropertiesApplied() {
        contextRunner
            .withPropertyValues(
                "core.docs.title=Test API",
                "core.docs.description=Test Description",
                "core.docs.version=v1",
                "core.docs.server-url=https://api.example.com",
                "core.docs.security.scheme-name=ApiAuth",
                "core.docs.security.scheme=bearer",
                "core.docs.security.bearer-format=JWT",
                "core.docs.security.description=Access token"
            )
            .run { context ->
                val openApi = context.getBean(OpenAPI::class.java)
                assertEquals("Test API", openApi.info.title)
                assertEquals("Test Description", openApi.info.description)
                assertEquals("v1", openApi.info.version)
                assertEquals("https://api.example.com", openApi.servers.first().url)

                val schemes = openApi.components?.securitySchemes
                assertNotNull(schemes)
                assertTrue(schemes.containsKey("ApiAuth"))

                val security = openApi.security
                assertNotNull(security)
                assertTrue(security.any { it.containsKey("ApiAuth") })
            }
    }

    @Test
    @DisplayName("보안 스키마를 비활성화하면 security 설정이 비어있다")
    fun securityDisabled() {
        contextRunner
            .withPropertyValues(
                "core.docs.security.enabled=false"
            )
            .run { context ->
                val openApi = context.getBean(OpenAPI::class.java)
                assertNull(openApi.components)
                assertNull(openApi.security)
            }
    }

    @Test
    @DisplayName("core.docs.enabled=false이면 OpenAPI 빈이 등록되지 않는다")
    fun openApiBeanDisabled() {
        contextRunner
            .withPropertyValues(
                "core.docs.enabled=false"
            )
            .run { context ->
                assertFalse(context.containsBean("openApi"))
            }
    }
}
