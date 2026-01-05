package rubit.coretest.coredata.controller

import jakarta.persistence.EntityManagerFactory
import org.hibernate.engine.config.spi.ConfigurationService
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rubit.coredata.config.CoreDataJpaProperties

@RestController
@RequestMapping("/api/data/settings")
class JpaSettingsController(
    private val entityManagerFactory: EntityManagerFactory,
    private val coreDataJpaProperties: CoreDataJpaProperties
) {
    @GetMapping
    fun settings(): JpaSettingsResponse {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor::class.java)
        val options = sessionFactory.sessionFactoryOptions
        val configurationService = sessionFactory.serviceRegistry.getService(ConfigurationService::class.java)
            ?: error("ConfigurationService not available")
        val settings = configurationService.settings

        return JpaSettingsResponse(
            configuredBatchSize = coreDataJpaProperties.batchSize,
            configuredFetchSize = coreDataJpaProperties.fetchSize,
            configuredTimeZone = coreDataJpaProperties.timeZone,
            physicalNamingStrategy = normalizeSetting(settings["hibernate.physical_naming_strategy"]),
            implicitNamingStrategy = normalizeSetting(settings["hibernate.implicit_naming_strategy"]),
            hibernateJdbcBatchSize = normalizeIntSetting(settings["hibernate.jdbc.batch_size"]),
            hibernateJdbcFetchSize = normalizeIntSetting(settings["hibernate.jdbc.fetch_size"]),
            hibernateJdbcTimeZone = normalizeSetting(settings["hibernate.jdbc.time_zone"]),
            jdbcBatchSize = options.jdbcBatchSize,
            jdbcFetchSize = options.jdbcFetchSize,
            jdbcTimeZone = options.jdbcTimeZone?.id
        )
    }

    private fun normalizeSetting(value: Any?): String? {
        return when (value) {
            null -> null
            is Class<*> -> value.name
            else -> value.toString()
        }
    }

    private fun normalizeIntSetting(value: Any?): Int? {
        return when (value) {
            null -> null
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }
}

data class JpaSettingsResponse(
    val configuredBatchSize: Int?,
    val configuredFetchSize: Int?,
    val configuredTimeZone: String?,
    val physicalNamingStrategy: String?,
    val implicitNamingStrategy: String?,
    val hibernateJdbcBatchSize: Int?,
    val hibernateJdbcFetchSize: Int?,
    val hibernateJdbcTimeZone: String?,
    val jdbcBatchSize: Int,
    val jdbcFetchSize: Int?,
    val jdbcTimeZone: String?
)
