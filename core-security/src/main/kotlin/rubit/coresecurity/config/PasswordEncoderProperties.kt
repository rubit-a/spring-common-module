package rubit.coresecurity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.password-encoder")
data class PasswordEncoderProperties(
    val enabled: Boolean = true,
    val strength: Int = 10
)
