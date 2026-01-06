package rubit.coresecurityoauth2.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rubit.coresecurity.config.JwtAutoConfiguration
import rubit.coresecurity.config.JwtProperties
import rubit.coresecurity.filter.JwtAuthenticationFilter
import rubit.coresecurity.jwt.JwtTokenProvider
import rubit.coresecurityoauth2.handler.OAuth2JwtFailureHandler
import rubit.coresecurityoauth2.handler.OAuth2JwtSuccessHandler
import rubit.coresecurityoauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository

@AutoConfiguration
@AutoConfigureBefore(JwtAutoConfiguration::class)
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2Properties::class, JwtProperties::class)
@ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "jwt", matchIfMissing = true)
@ConditionalOnProperty(prefix = "auth.oauth2", name = ["enabled"], havingValue = "true")
@ConditionalOnProperty(prefix = "jwt", name = ["secret-key"])
class OAuth2AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun authorizationRequestRepository(
        properties: OAuth2Properties
    ): HttpCookieOAuth2AuthorizationRequestRepository {
        return HttpCookieOAuth2AuthorizationRequestRepository(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    fun oauth2JwtSuccessHandler(
        jwtTokenProvider: JwtTokenProvider,
        jwtProperties: JwtProperties,
        oauth2Properties: OAuth2Properties,
        authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
        objectMapper: ObjectMapper
    ): AuthenticationSuccessHandler {
        return OAuth2JwtSuccessHandler(
            jwtTokenProvider,
            jwtProperties,
            oauth2Properties,
            authorizationRequestRepository,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean
    fun oauth2JwtFailureHandler(
        authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
        objectMapper: ObjectMapper
    ): AuthenticationFailureHandler {
        return OAuth2JwtFailureHandler(authorizationRequestRepository, objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
    fun oauth2SecurityFilterChain(
        http: HttpSecurity,
        authorizationRequestRepository: AuthorizationRequestRepository<OAuth2AuthorizationRequest>,
        successHandler: AuthenticationSuccessHandler,
        failureHandler: AuthenticationFailureHandler,
        jwtAuthenticationFilterProvider: ObjectProvider<JwtAuthenticationFilter>
    ): SecurityFilterChain {
        val jwtFilter = jwtAuthenticationFilterProvider.ifAvailable

        val configured = http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(authorizationRequestRepository)
                }
                oauth2.successHandler(successHandler)
                oauth2.failureHandler(failureHandler)
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        if (jwtFilter != null) {
            configured.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return configured.build()
    }
}
