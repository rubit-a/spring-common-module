package rubit.corelogging.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.OncePerRequestFilter
import org.aspectj.lang.annotation.Aspect
import rubit.corelogging.aop.LogExecutionTimeAspect
import rubit.corelogging.filter.RequestIdFilter
import rubit.corelogging.filter.TraceContextFilter

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

    @Bean
    @ConditionalOnClass(OncePerRequestFilter::class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(
        prefix = "core.logging.trace",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun traceContextFilter(properties: CoreLoggingProperties): TraceContextFilter {
        return TraceContextFilter(properties.trace)
    }

    @Bean
    @ConditionalOnClass(Aspect::class)
    @ConditionalOnProperty(
        prefix = "core.logging.aop",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    fun logExecutionTimeAspect(properties: CoreLoggingProperties): LogExecutionTimeAspect {
        return LogExecutionTimeAspect(properties.aop)
    }
}
