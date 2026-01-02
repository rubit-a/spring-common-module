package rubit.coreweb.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import rubit.coreweb.response.ApiErrorHandler
import rubit.coreweb.response.ApiResponseBodyAdvice

@AutoConfiguration
@ConditionalOnProperty(prefix = "core.web.response", name = ["enabled"], havingValue = "true")
class ResponseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiResponseBodyAdvice::class)
    fun apiResponseBodyAdvice(properties: CoreWebProperties): ApiResponseBodyAdvice {
        return ApiResponseBodyAdvice(properties)
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "core.web.response",
        name = ["error-enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(ApiErrorHandler::class)
    fun apiErrorHandler(): ApiErrorHandler {
        return ApiErrorHandler()
    }
}
