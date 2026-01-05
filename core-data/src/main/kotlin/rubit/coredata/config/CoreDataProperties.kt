package rubit.coredata.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.data")
data class CoreDataProperties(
    val auditingEnabled: Boolean = true
)
