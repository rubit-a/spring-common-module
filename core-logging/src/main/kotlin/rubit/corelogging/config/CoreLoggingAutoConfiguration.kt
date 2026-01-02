package rubit.corelogging.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.OncePerRequestFilter
import rubit.corelogging.filter.RequestIdFilter

@AutoConfiguration
@EnableConfigurationProperties(CoreLoggingProperties::class)
class CoreLoggingAutoConfiguration {

    @Bean
    @ConditionalOnClass(OncePerRequestFilter::class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(
        prefix = "core.logging.request-id",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun requestIdFilter(properties: CoreLoggingProperties): RequestIdFilter {
        return RequestIdFilter(properties.requestId)
    }
}
