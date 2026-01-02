package rubit.coreweb.test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(
    classes = [WebConfigTestApplication::class],
    properties = [
        "core.web.response.enabled=true",
        "core.web.jackson.enabled=true",
        "core.web.jackson.serialization-inclusion=NON_NULL",
        "core.web.jackson.naming-strategy=SNAKE_CASE",
        "core.web.jackson.fail-on-unknown-properties=true",
        "core.web.jackson.write-dates-as-timestamps=false",
        "core.web.format.enabled=true",
        "core.web.format.date=yyyyMMdd",
        "core.web.format.date-time=yyyy-MM-dd HH:mm:ss",
        "core.web.format.time=HHmmss",
        "core.web.cors.enabled=true",
        "core.web.cors.allowed-origin-patterns[0]=https://*.example.com"
    ]
)
@DisplayName("core-web Web/Jackson 설정 통합 테스트")
class WebConfigIntegrationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("JSON 네이밍/날짜 포맷/NULL 제외 설정이 적용된다")
    fun appliesJsonNamingAndDateFormat() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/web-config/dto")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.created_at").value("2025-01-01 10:15:30"))
            .andExpect(jsonPath("$.data.optional_note").doesNotExist())
    }

    @Test
    @DisplayName("요청 파라미터 날짜 포맷이 적용된다")
    fun appliesRequestParamDateFormat() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/web-config/date")
                .param("date", "20250131")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.value").value("2025-01-31"))
    }

    @Test
    @DisplayName("미등록 필드가 있으면 400으로 실패한다")
    fun failsOnUnknownProperties() {
        val payload = """{"id":1,"createdAt":"2025-01-01 10:15:30","extra":"x"}"""
        mockMvc.perform(
            MockMvcRequestBuilders.post("/web-config/dto")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"))
    }

    @Test
    @DisplayName("CORS 프리플라이트 요청이 허용된다")
    fun allowsCorsPreflight() {
        mockMvc.perform(
            MockMvcRequestBuilders.options("/web-config/dto")
                .header("Origin", "https://api.example.com")
                .header("Access-Control-Request-Method", "POST")
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Access-Control-Allow-Origin", "https://api.example.com"))
    }
}

@SpringBootApplication
class WebConfigTestApplication

@RestController
@RequestMapping("/web-config")
class WebConfigTestController {
    @GetMapping("/dto")
    fun dto(): SampleDto {
        return SampleDto(
            id = 1,
            createdAt = LocalDateTime.of(2025, 1, 1, 10, 15, 30),
            optionalNote = null
        )
    }

    @PostMapping("/dto")
    fun dtoPost(@RequestBody request: SampleRequest): SampleRequest {
        return request
    }

    @GetMapping("/date")
    fun date(@RequestParam date: LocalDate): Map<String, String> {
        return mapOf("value" to date.format(DateTimeFormatter.ISO_DATE))
    }
}

data class SampleDto(
    val id: Long,
    val createdAt: LocalDateTime,
    val optionalNote: String? = null
)

data class SampleRequest(
    val id: Long,
    val createdAt: LocalDateTime
)
