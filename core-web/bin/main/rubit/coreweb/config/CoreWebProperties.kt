package rubit.coreweb.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.web")
data class CoreWebProperties(
    val response: ResponseProperties = ResponseProperties()
)

data class ResponseProperties(
    val enabled: Boolean = false,
    val wrapNull: Boolean = true,
    val errorEnabled: Boolean = true
)
