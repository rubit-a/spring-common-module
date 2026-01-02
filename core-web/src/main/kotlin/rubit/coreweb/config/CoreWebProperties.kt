package rubit.coreweb.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.web")
data class CoreWebProperties(
    val response: ResponseProperties = ResponseProperties(),
    val jackson: JacksonProperties = JacksonProperties(),
    val cors: CorsProperties = CorsProperties(),
    val format: FormatProperties = FormatProperties()
)

data class ResponseProperties(
    val enabled: Boolean = false,
    val wrapNull: Boolean = true,
    val errorEnabled: Boolean = true
)

data class JacksonProperties(
    val enabled: Boolean = false,
    val timeZone: String? = null,
    val serializationInclusion: String = "NON_NULL",
    val dateFormat: String? = null,
    val namingStrategy: String? = null,
    val failOnUnknownProperties: Boolean = false,
    val writeDatesAsTimestamps: Boolean = false
)

data class CorsProperties(
    val enabled: Boolean = false,
    val pathPattern: String = "/**",
    val allowedOrigins: List<String> = emptyList(),
    val allowedOriginPatterns: List<String> = listOf("*"),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
    val allowedHeaders: List<String> = listOf("*"),
    val exposedHeaders: List<String> = emptyList(),
    val allowCredentials: Boolean = false,
    val maxAge: Long = 3600
)

data class FormatProperties(
    val enabled: Boolean = false,
    val date: String = "yyyy-MM-dd",
    val dateTime: String = "yyyy-MM-dd'T'HH:mm:ss",
    val time: String = "HH:mm:ss"
)
