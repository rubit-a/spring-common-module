package rubit.coredocs.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.docs")
data class DocsProperties(
    val enabled: Boolean = true,
    val title: String = "API",
    val description: String? = null,
    val version: String? = null,
    val serverUrl: String? = null,
    val security: SecurityProperties = SecurityProperties()
)

data class SecurityProperties(
    val enabled: Boolean = true,
    val schemeName: String = "BearerAuth",
    val scheme: String = "bearer",
    val bearerFormat: String = "JWT",
    val description: String? = null
)
