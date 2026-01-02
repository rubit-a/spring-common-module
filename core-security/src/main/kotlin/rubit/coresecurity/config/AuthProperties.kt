package rubit.coresecurity.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "auth")
@Validated
data class AuthProperties(
    val mode: AuthMode = AuthMode.JWT
)

enum class AuthMode {
    JWT,
    SESSION
}
