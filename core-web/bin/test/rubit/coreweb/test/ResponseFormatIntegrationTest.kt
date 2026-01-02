package rubit.coreweb.test

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest(
    classes = [TestApplication::class],
    properties = ["core.web.response.enabled=true"]
)
@DisplayName("core-web 응답 포맷/예외 처리 통합 테스트")
class ResponseFormatIntegrationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("정상 응답은 공통 포맷으로 감싼다")
    fun wrapsSuccessResponse() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/test/ok")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.value").value("pong"))
            .andExpect(jsonPath("$.error").value(nullValue()))
    }

    @Test
    @DisplayName("검증 오류는 공통 에러 포맷으로 반환한다")
    fun wrapsValidationError() {
        val payload = """{"name": ""}"""
        mockMvc.perform(
            MockMvcRequestBuilders.post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details.fields.name").exists())
    }

    @Test
    @DisplayName("IllegalArgumentException은 400으로 변환한다")
    fun wrapsIllegalArgumentException() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/test/illegal")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
    }

}

@SpringBootApplication
class TestApplication

@RestController
@RequestMapping("/test")
class TestController {
    @GetMapping("/ok")
    fun ok(): Map<String, String> {
        return mapOf("value" to "pong")
    }

    @PostMapping("/validate")
    fun validate(@Valid @RequestBody request: TestRequest): TestRequest {
        return request
    }

    @GetMapping("/illegal")
    fun illegal(): String {
        throw IllegalArgumentException("invalid request")
    }
}

data class TestRequest(
    @field:NotBlank val name: String
)
