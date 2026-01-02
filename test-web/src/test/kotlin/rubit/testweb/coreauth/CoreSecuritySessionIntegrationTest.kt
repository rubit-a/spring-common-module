package rubit.testweb.coreauth

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import rubit.testweb.coreauth.dto.LoginRequest
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@TestPropertySource(properties = ["auth.mode=session"])
@DisplayName("core-security Session 통합 테스트")
class CoreSecuritySessionIntegrationTest {

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
    @DisplayName("세션 로그인 후 보호된 엔드포인트에 접근한다")
    fun loginAndAccessProtectedEndpoint() {
        val session = login("testuser", "password123")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/me")
                .session(session)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authenticated").value(true))
    }

    @Test
    @DisplayName("관리자 계정은 admin 엔드포인트에 접근할 수 있다")
    fun adminCanAccessAdminEndpoint() {
        val session = login("admin", "admin123")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/admin")
                .session(session)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.level").value("ADMIN_ONLY"))
    }

    @Test
    @DisplayName("일반 계정은 admin 엔드포인트에 접근할 수 없다")
    fun userCannotAccessAdminEndpoint() {
        val session = login("testuser", "password123")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/users/admin")
                .session(session)
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    private fun login(username: String, password: String): MockHttpSession {
        val payload = objectMapper.writeValueAsString(LoginRequest(username, password))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/session/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        return result.request.session as MockHttpSession
    }
}
