package rubit.coredata.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(HibernatePropertiesCustomizer::class)
@EnableConfigurationProperties(CoreDataJpaProperties::class)
class CoreDataJpaAutoConfiguration {

    @Bean
    fun coreDataHibernatePropertiesCustomizer(
        properties: CoreDataJpaProperties
    ): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { hibernateProperties ->
            properties.physicalNamingStrategy?.let {
                hibernateProperties["hibernate.physical_naming_strategy"] = it
            }
            properties.implicitNamingStrategy?.let {
                hibernateProperties["hibernate.implicit_naming_strategy"] = it
            }
            properties.batchSize?.let { batchSize ->
                if (batchSize > 0) {
                    hibernateProperties["hibernate.jdbc.batch_size"] = batchSize
                }
            }
            properties.fetchSize?.let { fetchSize ->
                if (fetchSize > 0) {
                    hibernateProperties["hibernate.jdbc.fetch_size"] = fetchSize
                }
            }
            properties.timeZone?.let {
                hibernateProperties["hibernate.jdbc.time_zone"] = it
            }
        }
    }
}
