package rubit.corelogging.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.logging")
data class CoreLoggingProperties(
    val requestId: RequestIdProperties = RequestIdProperties()
)

data class RequestIdProperties(
    val enabled: Boolean = true,
    val header: String = "X-Request-Id",
    val mdcKey: String = "requestId",
    val generateIfMissing: Boolean = true
)
