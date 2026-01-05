package rubit.coretest.coredata.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

@Configuration
class AuditorConfig {
    @Bean
    fun auditorAware(): AuditorAware<String> = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            Optional.of("system")
        } else {
            Optional.ofNullable(authentication.name)
        }
    }
}
