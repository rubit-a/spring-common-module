package rubit.coresecurity.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rubit.coresecurity.filter.JwtAuthenticationFilter
import rubit.coresecurity.jwt.JwtTokenProvider

@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class)
@ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "jwt", matchIfMissing = true)
@ConditionalOnProperty(prefix = "jwt", name = ["secret-key"])
class JwtAutoConfiguration(
    private val jwtProperties: JwtProperties
) {

    @Bean
    @ConditionalOnMissingBean
    fun jwtTokenProvider(): JwtTokenProvider {
        return JwtTokenProvider(jwtProperties)
    }

    @Bean
    @ConditionalOnMissingBean
    fun jwtAuthenticationFilter(jwtTokenProvider: JwtTokenProvider): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtTokenProvider)
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
    fun jwtSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
