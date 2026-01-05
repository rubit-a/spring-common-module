package rubit.coresecurity.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@AutoConfiguration
@ConditionalOnClass(PasswordEncoder::class)
@EnableConfigurationProperties(PasswordEncoderProperties::class)
class PasswordEncoderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder::class)
    @ConditionalOnProperty(
        prefix = "auth.password-encoder",
        name = ["enabled"],
        havingValue = "true",
        matchIfMissing = true
    )
    fun passwordEncoder(properties: PasswordEncoderProperties): PasswordEncoder {
        return BCryptPasswordEncoder(properties.strength)
    }
}
