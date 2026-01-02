package rubit.coresecurity.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val accessTokenExpiration: Long = 3600000, // 1 hour in milliseconds
    val refreshTokenExpiration: Long = 604800000, // 7 days in milliseconds
    val issuer: String = "core-security"
)
