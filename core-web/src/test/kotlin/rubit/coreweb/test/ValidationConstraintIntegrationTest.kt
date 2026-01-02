package rubit.coreweb.test

import jakarta.validation.Valid
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
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import rubit.coreweb.validation.DateRange
import rubit.coreweb.validation.DateRangeType
import rubit.coreweb.validation.EnumValue
import rubit.coreweb.validation.PatternValue
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(
    classes = [ValidationTestApplication::class],
    properties = ["core.web.response.enabled=true"]
)
@DisplayName("core-web 커스텀 검증 유틸 통합 테스트")
class ValidationConstraintIntegrationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    @DisplayName("EnumValue 검증 실패 시 경로가 정규화된다")
    fun enumValueViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/validation/enum")
                .param("status", "UNKNOWN")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.details.violations.status").exists())
    }

    @Test
    @DisplayName("DateRange 검증 실패 시 경로가 정규화된다")
    fun dateRangeViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/validation/date")
                .param("date", "2024-12-31")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.details.violations.date").exists())
    }

    @Test
    @DisplayName("PatternValue 검증 실패 시 경로가 정규화된다")
    fun patternValueViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/validation/pattern")
                .param("code", "ABC123")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.details.violations.code").exists())
    }

    @Test
    @DisplayName("문자열 날짜 검증 실패 시 경로가 정규화된다")
    fun dateTextViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/validation/date-text")
                .param("dateText", "2024-01-01")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.details.violations.dateText").exists())
    }

    @Test
    @DisplayName("요청 바디 검증에 커스텀 제약이 적용된다")
    fun appliesCustomConstraintOnBody() {
        val payload = """{"status":"UNKNOWN","createdAt":"2025-01-01T10:00:00"}"""
        mockMvc.perform(
            MockMvcRequestBuilders.post("/validation/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.details.fields.status").exists())
    }
}

@SpringBootApplication
class ValidationTestApplication

@Validated
@RestController
@RequestMapping("/validation")
class ValidationTestController {
    @GetMapping("/enum")
    fun enumParam(
        @RequestParam
        @EnumValue(enumClass = SampleStatus::class, allowNull = false)
        status: String
    ): String {
        return status
    }

    @GetMapping("/date")
    fun dateParam(
        @RequestParam
        @DateRange(min = "2025-01-01", max = "2025-12-31")
        date: LocalDate
    ): String {
        return date.toString()
    }

    @GetMapping("/pattern")
    fun patternParam(
        @RequestParam
        @PatternValue(regexp = "^[a-z]+$")
        code: String
    ): String {
        return code
    }

    @GetMapping("/date-text")
    fun dateTextParam(
        @RequestParam
        @DateRange(
            min = "2025-01-01",
            max = "2025-12-31",
            type = DateRangeType.DATE,
            allowNull = false
        )
        dateText: String
    ): String {
        return dateText
    }

    @PostMapping("/body")
    fun body(@Valid @RequestBody request: ValidationRequest): ValidationRequest {
        return request
    }
}

enum class SampleStatus {
    ACTIVE,
    INACTIVE
}

data class ValidationRequest(
    @field:EnumValue(enumClass = SampleStatus::class)
    val status: String,
    val createdAt: LocalDateTime
)
