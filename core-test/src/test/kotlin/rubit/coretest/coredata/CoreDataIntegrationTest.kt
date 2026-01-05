package rubit.coretest.coredata

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
import rubit.coretest.coredata.controller.SampleCreateRequest
import rubit.coretest.coredata.controller.SampleUpdateRequest
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@DisplayName("core-data 통합 테스트")
class CoreDataIntegrationTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @DisplayName("샘플 생성/수정 시 auditing 필드가 채워진다")
    fun auditingFieldsArePopulated() {
        val accessToken = login("testuser", "password123")

        val createPayload = objectMapper.writeValueAsString(SampleCreateRequest(name = "alpha"))
        val createResult = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/data/samples")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPayload)
        )
            .andExpect(status().isOk)
            .andReturn()

        val createdData = extractDataNode(createResult.response.contentAsString)
        val sampleId = createdData.path("id").asLong()
        assertTrue(sampleId > 0)
        assertEquals("alpha", createdData.path("name").asText())
        assertEquals("testuser", createdData.path("createdBy").asText())
        assertEquals("testuser", createdData.path("updatedBy").asText())
        assertFalse(createdData.path("createdAt").isNull)
        assertFalse(createdData.path("updatedAt").isNull)

        val updatePayload = objectMapper.writeValueAsString(SampleUpdateRequest(name = "beta"))
        val updateResult = mockMvc.perform(
            MockMvcRequestBuilders.patch("/api/data/samples/$sampleId")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload)
        )
            .andExpect(status().isOk)
            .andReturn()

        val updatedData = extractDataNode(updateResult.response.contentAsString)
        assertEquals(sampleId, updatedData.path("id").asLong())
        assertEquals("beta", updatedData.path("name").asText())
        assertEquals("testuser", updatedData.path("createdBy").asText())
        assertEquals("testuser", updatedData.path("updatedBy").asText())
        assertFalse(updatedData.path("updatedAt").isNull)
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
        return tokenNode.asText()
    }

    private fun extractDataNode(content: String): JsonNode {
        val root = objectMapper.readTree(content)
        val dataNode = root.path("data")
        return if (dataNode.isMissingNode || dataNode.isNull) root else dataNode
    }
}
