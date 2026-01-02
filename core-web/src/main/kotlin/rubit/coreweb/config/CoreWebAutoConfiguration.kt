package rubit.coreweb.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(CoreWebProperties::class)
class CoreWebAutoConfiguration
