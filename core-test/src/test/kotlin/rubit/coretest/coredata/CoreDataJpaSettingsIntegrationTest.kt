package rubit.coretest.coredata

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import rubit.coretest.coreauth.dto.LoginRequest
import tools.jackson.databind.ObjectMapper

@SpringBootTest(
    properties = [
        "core.data.jpa.physical-naming-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
        "core.data.jpa.implicit-naming-strategy=org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
        "core.data.jpa.batch-size=25",
        "core.data.jpa.fetch-size=50",
        "core.data.jpa.time-zone=UTC"
    ]
)
@DisplayName("core-data JPA 설정 통합 테스트")
class CoreDataJpaSettingsIntegrationTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("JPA 설정이 Hibernate 옵션으로 반영된다")
    fun jpaSettingsAreApplied() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()

        val accessToken = login("testuser", "password123")

        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/data/settings")
                .header("Authorization", "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()

        val data = extractDataNode(result.response.contentAsString)

        assertEquals(
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
            data.path("physicalNamingStrategy").asString()
        )
        assertEquals(
            "org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
            data.path("implicitNamingStrategy").asString()
        )
        assertEquals(25, data.path("configuredBatchSize").asInt())
        assertEquals(50, data.path("configuredFetchSize").asInt())
        assertEquals("UTC", data.path("configuredTimeZone").asString())
        assertEquals(25, data.path("hibernateJdbcBatchSize").asInt())
        assertEquals(50, data.path("hibernateJdbcFetchSize").asInt())
        assertEquals("UTC", data.path("hibernateJdbcTimeZone").asString())
        assertEquals(25, data.path("jdbcBatchSize").asInt())
        assertEquals(50, data.path("jdbcFetchSize").asInt())
        assertEquals("UTC", data.path("jdbcTimeZone").asString())
    }

    private fun login(username: String, password: String): String {
        val payload = objectMapper.writeValueAsString(LoginRequest(username, password))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isOk)
            .andReturn()

        val root = objectMapper.readTree(result.response.contentAsString)
        val dataNode = root.path("data")
        val tokenNode = if (dataNode.isMissingNode || dataNode.isNull) root.path("accessToken") else dataNode.path("accessToken")
        return tokenNode.asString()
    }

    private fun extractDataNode(content: String): tools.jackson.databind.JsonNode {
        val root = objectMapper.readTree(content)
        val dataNode = root.path("data")
        return if (dataNode.isMissingNode || dataNode.isNull) root else dataNode
    }
}
