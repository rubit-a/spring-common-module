package rubit.coretest.coreweb

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@SpringBootTest(properties = ["core.web.response.enabled=true"])
@DisplayName("core-web 응답 포맷 통합 테스트")
class CoreWebResponseIntegrationTest {

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
    @DisplayName("공개 엔드포인트 응답은 공통 포맷으로 감싼다")
    fun wrapsPublicResponse() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/hello")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").value("Hello! This is a public endpoint."))
            .andExpect(jsonPath("$.error").value(nullValue()))
    }

    @Test
    @DisplayName("검증 오류는 공통 에러 포맷으로 반환한다")
    fun wrapsValidationError() {
        val payload = """{"name": ""}"""
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/public/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.fields.name").exists())
    }

    @Test
    @DisplayName("IllegalArgumentException은 공통 에러 포맷으로 반환한다")
    fun wrapsIllegalArgumentException() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/test/illegal")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
            .andExpect(jsonPath("$.error.message").value("invalid request"))
    }
}

@RestController
@RequestMapping("/api/public/test")
class CoreWebTestController {

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody request: CoreWebTestRequest): CoreWebTestRequest {
        return request
    }

    @GetMapping("/illegal")
    fun illegal(): String {
        throw IllegalArgumentException("invalid request")
    }
}

data class CoreWebTestRequest(
    @field:NotBlank val name: String
)
