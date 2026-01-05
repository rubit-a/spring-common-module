package rubit.coredata.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import rubit.coredata.audit.DefaultAuditorAware

@AutoConfiguration
@ConditionalOnClass(EnableJpaAuditing::class)
@ConditionalOnProperty(prefix = "core.data", name = ["auditing-enabled"], havingValue = "true", matchIfMissing = true)
@EnableJpaAuditing
class JpaAuditingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(AuditorAware::class)
    fun auditorAware(): AuditorAware<String> = DefaultAuditorAware()
}
