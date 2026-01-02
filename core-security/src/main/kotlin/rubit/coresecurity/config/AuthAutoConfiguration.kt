package rubit.coresecurity.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties::class)
class AuthAutoConfiguration
