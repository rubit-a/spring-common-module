package rubit.coredocs.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(OpenAPI::class)
@EnableConfigurationProperties(DocsProperties::class)
@ConditionalOnProperty(prefix = "core.docs", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class CoreDocsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun openApi(properties: DocsProperties): OpenAPI {
        val info = Info().title(properties.title)
        properties.description?.let { info.description(it) }
        properties.version?.let { info.version(it) }

        val openApi = OpenAPI().info(info)
        properties.serverUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { openApi.addServersItem(Server().url(it)) }

        if (properties.security.enabled) {
            val schemeName = properties.security.schemeName
            val scheme = SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(properties.security.scheme)
                .bearerFormat(properties.security.bearerFormat)
            properties.security.description?.let { scheme.description(it) }

            openApi.components(
                Components().addSecuritySchemes(schemeName, scheme)
            )
            openApi.addSecurityItem(SecurityRequirement().addList(schemeName))
        }

        return openApi
    }
}
