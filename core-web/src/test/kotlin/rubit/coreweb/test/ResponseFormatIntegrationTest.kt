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
import org.springframework.web.bind.annotation.RequestParam
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

    @Test
    @DisplayName("파라미터 누락은 MISSING_PARAMETER로 변환한다")
    fun wrapsMissingParameter() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/test/param")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("MISSING_PARAMETER"))
    }

    @Test
    @DisplayName("타입 불일치는 TYPE_MISMATCH로 변환한다")
    fun wrapsTypeMismatch() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/test/param")
                .param("id", "abc")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("TYPE_MISMATCH"))
    }

    @Test
    @DisplayName("허용되지 않은 메서드는 METHOD_NOT_ALLOWED로 변환한다")
    fun wrapsMethodNotAllowed() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/test/ok")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"))
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 UNSUPPORTED_MEDIA_TYPE으로 변환한다")
    fun wrapsUnsupportedMediaType() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/test/validate")
                .contentType(MediaType.TEXT_PLAIN)
                .content("name")
        )
            .andExpect(status().isUnsupportedMediaType)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"))
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

    @PostMapping("/validate", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun validate(@Valid @RequestBody request: TestRequest): TestRequest {
        return request
    }

    @GetMapping("/illegal")
    fun illegal(): String {
        throw IllegalArgumentException("invalid request")
    }

    @GetMapping("/param")
    fun param(@RequestParam id: Long): String {
        return id.toString()
    }
}

data class TestRequest(
    @field:NotBlank val name: String
)
