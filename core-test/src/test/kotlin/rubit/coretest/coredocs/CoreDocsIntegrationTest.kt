package rubit.coretest.coredocs

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@DisplayName("core-docs 통합 테스트")
class CoreDocsIntegrationTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @DisplayName("docs 설정 응답을 확인한다")
    fun docsConfigEndpointReturnsProperties() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/docs/config"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Core Test Docs"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.version").value("v1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.serverUrl").value("http://localhost"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.securityEnabled").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.schemeName").value("BearerAuth"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.scheme").value("bearer"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bearerFormat").value("JWT"))
    }

    @Test
    @DisplayName("OpenAPI 문서가 노출된다")
    fun openApiDocsEndpointAvailable() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/docs"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.info.title").value("Core Test Docs"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.components.securitySchemes.BearerAuth.type").value("http"))
    }
}
