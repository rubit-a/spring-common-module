package rubit.corelogging.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.logging")
data class CoreLoggingProperties(
    val requestId: RequestIdProperties = RequestIdProperties(),
    val trace: TraceProperties = TraceProperties(),
    val aop: AopLoggingProperties = AopLoggingProperties()
)

data class RequestIdProperties(
    val enabled: Boolean = true,
    val header: String = "X-Request-Id",
    val mdcKey: String = "requestId",
    val generateIfMissing: Boolean = true
)

data class TraceProperties(
    val enabled: Boolean = true,
    val mdcTraceIdKey: String = "traceId",
    val mdcSpanIdKey: String = "spanId",
    val generateIfMissing: Boolean = true
)

data class AopLoggingProperties(
    val enabled: Boolean = false,
    val slowThresholdMs: Long = 0,
    val logArgs: Boolean = false,
    val logResult: Boolean = false
)
