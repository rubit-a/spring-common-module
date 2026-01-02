package rubit.commonauth.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@AutoConfiguration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "session")
class SessionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
    fun sessionSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests { auth -> auth.anyRequest().authenticated() }
            .build()
    }
}
