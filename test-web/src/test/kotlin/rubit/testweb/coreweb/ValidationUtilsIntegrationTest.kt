package rubit.testweb.coreweb

import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rubit.testweb.TestWebApplication

@SpringBootTest(
    classes = [TestWebApplication::class],
    properties = ["core.web.response.enabled=true"]
)
@AutoConfigureMockMvc
@DisplayName("test-web 검증 유틸 통합 테스트")
class ValidationUtilsIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("EnumValue 검증 실패 시 파라미터 경로가 정규화된다")
    fun enumValueViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/validation/enum")
                .param("status", "UNKNOWN")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("Validation failed"))
            .andExpect(jsonPath("$.error.details.violations.status").isArray())
            .andExpect(jsonPath("$.error.details.violations.status").exists())
            .andExpect(jsonPath("$.error.details.violations.status[0]").isString())
    }

    @Test
    @DisplayName("DateRange 검증 실패 시 파라미터 경로가 정규화된다")
    fun dateRangeViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/validation/date")
                .param("date", "2024-12-31")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("Validation failed"))
            .andExpect(jsonPath("$.error.details.violations.date").isArray())
            .andExpect(jsonPath("$.error.details.violations.date").exists())
            .andExpect(jsonPath("$.error.details.violations.date[0]").isString())
    }

    @Test
    @DisplayName("PatternValue 검증 실패 시 파라미터 경로가 정규화된다")
    fun patternValueViolationPathNormalized() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/validation/pattern")
                .param("code", "ABC123")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("Validation failed"))
            .andExpect(jsonPath("$.error.details.violations.code").isArray())
            .andExpect(jsonPath("$.error.details.violations.code").exists())
            .andExpect(jsonPath("$.error.details.violations.code[0]").isString())
    }

    @Test
    @DisplayName("요청 바디에 커스텀 제약이 적용된다")
    fun appliesCustomConstraintOnBody() {
        val payload = """{"status":"UNKNOWN","dateText":"2024-01-01"}"""
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/public/validation/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.message").value("Validation failed"))
            .andExpect(jsonPath("$.error.details.fields.status").isArray())
            .andExpect(jsonPath("$.error.details.fields.status").exists())
            .andExpect(jsonPath("$.error.details.fields.status[0]").isString())
            .andExpect(jsonPath("$.error.details.fields.dateText").isArray())
            .andExpect(jsonPath("$.error.details.fields.dateText").exists())
            .andExpect(jsonPath("$.error.details.fields.dateText[0]").isString())
    }
}
