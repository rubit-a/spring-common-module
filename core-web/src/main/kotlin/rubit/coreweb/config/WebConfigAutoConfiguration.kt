package rubit.coreweb.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.PropertyNamingStrategy
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer
import tools.jackson.databind.module.SimpleModule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.TimeZone

@AutoConfiguration
class WebConfigAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "core.web.jackson", name = ["enabled"], havingValue = "true")
    fun jacksonCustomizer(
        properties: CoreWebProperties
    ): JsonMapperBuilderCustomizer {
        val jackson = properties.jackson
        val format = properties.format
        return JsonMapperBuilderCustomizer { builder ->
            jackson.timeZone?.takeIf { it.isNotBlank() }?.let {
                builder.defaultTimeZone(TimeZone.getTimeZone(it))
            }
            jackson.dateFormat?.takeIf { it.isNotBlank() }?.let {
                builder.defaultDateFormat(SimpleDateFormat(it))
            }
            parseInclusion(jackson.serializationInclusion)?.let { inclusion ->
                builder.changeDefaultPropertyInclusion {
                    JsonInclude.Value.construct(inclusion, inclusion)
                }
            }
            parseNamingStrategy(jackson.namingStrategy)?.let { builder.propertyNamingStrategy(it) }

            if (jackson.failOnUnknownProperties) {
                builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            } else {
                builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }

            if (jackson.writeDatesAsTimestamps) {
                builder.enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            } else {
                builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            }

            builder.addModule(buildJavaTimeModule(format))
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "core.web.cors", name = ["enabled"], havingValue = "true")
    fun corsConfigurer(
        properties: CoreWebProperties
    ): WebMvcConfigurer {
        val cors = properties.cors
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val mapping = registry.addMapping(cors.pathPattern)
                    .allowedMethods(*cors.allowedMethods.toTypedArray())
                    .allowedHeaders(*cors.allowedHeaders.toTypedArray())
                    .exposedHeaders(*cors.exposedHeaders.toTypedArray())
                    .allowCredentials(cors.allowCredentials)
                    .maxAge(cors.maxAge)

                if (cors.allowedOriginPatterns.isNotEmpty()) {
                    mapping.allowedOriginPatterns(*cors.allowedOriginPatterns.toTypedArray())
                } else if (cors.allowedOrigins.isNotEmpty()) {
                    mapping.allowedOrigins(*cors.allowedOrigins.toTypedArray())
                }
            }
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "core.web.format", name = ["enabled"], havingValue = "true")
    fun formatterConfigurer(
        properties: CoreWebProperties
    ): WebMvcConfigurer {
        val format = properties.format
        return object : WebMvcConfigurer {
            override fun addFormatters(registry: FormatterRegistry) {
                val registrar = DateTimeFormatterRegistrar()
                registrar.setDateFormatter(DateTimeFormatter.ofPattern(format.date))
                registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern(format.dateTime))
                registrar.setTimeFormatter(DateTimeFormatter.ofPattern(format.time))
                registrar.registerFormatters(registry)
            }
        }
    }

    private fun buildJavaTimeModule(
        format: FormatProperties
    ): SimpleModule {
        val module = SimpleModule()
        if (format.enabled) {
            val dateFormatter = DateTimeFormatter.ofPattern(format.date)
            val dateTimeFormatter = DateTimeFormatter.ofPattern(format.dateTime)
            val timeFormatter = DateTimeFormatter.ofPattern(format.time)
            module.addSerializer(LocalDate::class.java, LocalDateSerializer(dateFormatter))
            module.addDeserializer(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
            module.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
            module.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
            module.addSerializer(LocalTime::class.java, LocalTimeSerializer(timeFormatter))
            module.addDeserializer(LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
        }
        return module
    }

    private fun parseInclusion(value: String?): JsonInclude.Include? {
        val normalized = value?.trim()?.uppercase()
        return when (normalized) {
            "ALWAYS" -> JsonInclude.Include.ALWAYS
            "NON_NULL" -> JsonInclude.Include.NON_NULL
            "NON_EMPTY" -> JsonInclude.Include.NON_EMPTY
            "NON_DEFAULT" -> JsonInclude.Include.NON_DEFAULT
            null, "" -> null
            else -> null
        }
    }

    private fun parseNamingStrategy(value: String?): PropertyNamingStrategy? {
        return when (value?.trim()?.uppercase()) {
            "SNAKE_CASE" -> PropertyNamingStrategies.SNAKE_CASE
            "LOWER_CAMEL_CASE" -> PropertyNamingStrategies.LOWER_CAMEL_CASE
            "UPPER_CAMEL_CASE" -> PropertyNamingStrategies.UPPER_CAMEL_CASE
            "LOWER_DOT_CASE" -> PropertyNamingStrategies.LOWER_DOT_CASE
            "KEBAB_CASE" -> PropertyNamingStrategies.KEBAB_CASE
            null, "" -> null
            else -> null
        }
    }
}
