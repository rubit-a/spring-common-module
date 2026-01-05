package rubit.coredata.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.data.jpa")
data class CoreDataJpaProperties(
    val physicalNamingStrategy: String? = null,
    val implicitNamingStrategy: String? = null,
    val batchSize: Int? = null,
    val fetchSize: Int? = null,
    val timeZone: String? = null
)
