package rubit.coretest.coreauth

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import rubit.coresecurity.config.JwtKeyFormat
import rubit.coresecurity.config.JwtProperties
import rubit.coresecurity.jwt.JwtTokenProvider
import rubit.coretest.coreauth.dto.LoginRequest
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.TimeUnit

@SpringBootTest(
    properties = [
        "auth.mode=jwt",
        "jwt.secret-key=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
        "jwt.secret-key-format=BASE64",
        "jwt.issuer=core-test",
        "jwt.audience=core-test-audience",
        "jwt.clock-skew-seconds=5",
        "jwt.access-token-expiration=1"
    ]
)
@DisplayName("core-security JWT 설정 통합 테스트")
class CoreSecurityJwtConfigIntegrationTest {

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
    @DisplayName("JWT 설정이 응답으로 노출된다")
    fun jwtConfigIsReturned() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/config"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.issuer").value("core-test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.audience").value("core-test-audience"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.clockSkewSeconds").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.secretKeyFormat").value("BASE64"))
    }

    @Test
    @DisplayName("audience가 다른 토큰은 검증을 실패한다")
    fun validateTokenWithDifferentAudience() {
        val mismatchProvider = JwtTokenProvider(
            JwtProperties(
                secretKey = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=",
                issuer = "core-test",
                audience = "other-audience",
                clockSkewSeconds = 5,
                secretKeyFormat = JwtKeyFormat.BASE64
            )
        )
        val token = mismatchProvider.generateAccessToken("testuser", listOf("ROLE_USER"))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/auth/validate")
                .param("token", token)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(false))
    }

    @Test
    @DisplayName("clock skew 범위 내 만료된 토큰은 검증을 통과한다")
    fun validateTokenWithinClockSkew() {
        val accessToken = login("testuser", "password123")
        TimeUnit.MILLISECONDS.sleep(10)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/auth/validate")
                .param("token", accessToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.valid").value(true))
    }

    private fun login(username: String, password: String): String {
        val payload = objectMapper.writeValueAsString(LoginRequest(username, password))

        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseJson = objectMapper.readTree(result.response.contentAsString)
        val tokenNode = responseJson.get("accessToken")
        return tokenNode?.asString() ?: error("Missing accessToken in response")
    }
}
