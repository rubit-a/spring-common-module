package rubit.coredata.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

@DisplayName("CoreDataJpaAutoConfiguration 테스트")
class CoreDataJpaAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CoreDataJpaAutoConfiguration::class.java))

    @Test
    @DisplayName("설정된 JPA 속성이 Hibernate 속성으로 반영된다")
    fun appliesJpaProperties() {
        contextRunner
            .withPropertyValues(
                "core.data.jpa.physical-naming-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "core.data.jpa.implicit-naming-strategy=org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
                "core.data.jpa.batch-size=25",
                "core.data.jpa.fetch-size=50",
                "core.data.jpa.time-zone=UTC"
            )
            .run { context ->
                val customizer = context.getBean(HibernatePropertiesCustomizer::class.java)
                val hibernateProperties = mutableMapOf<String, Any>()

                customizer.customize(hibernateProperties)

                assertEquals(
                    "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                    hibernateProperties["hibernate.physical_naming_strategy"]
                )
                assertEquals(
                    "org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
                    hibernateProperties["hibernate.implicit_naming_strategy"]
                )
                assertEquals(25, hibernateProperties["hibernate.jdbc.batch_size"])
                assertEquals(50, hibernateProperties["hibernate.jdbc.fetch_size"])
                assertEquals("UTC", hibernateProperties["hibernate.jdbc.time_zone"])
            }
    }

    @Test
    @DisplayName("설정이 없으면 Hibernate 속성이 비어 있다")
    fun noPropertiesWhenNotConfigured() {
        contextRunner.run { context ->
            val customizer = context.getBean(HibernatePropertiesCustomizer::class.java)
            val hibernateProperties = mutableMapOf<String, Any>()

            customizer.customize(hibernateProperties)

            assertTrue(hibernateProperties.isEmpty())
        }
    }
}
