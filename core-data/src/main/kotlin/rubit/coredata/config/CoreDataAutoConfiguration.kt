package rubit.coredata.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(CoreDataProperties::class)
class CoreDataAutoConfiguration
