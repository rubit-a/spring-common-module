package rubit.coretest.corelogging

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rubit.coretest.CoreTestApplication

@SpringBootTest(
    classes = [CoreTestApplication::class],
    properties = [
        "core.web.response.enabled=true",
        "core.logging.request-id.enabled=true",
        "core.logging.request-id.header=X-Request-Id",
        "core.logging.request-id.generate-if-missing=true"
    ]
)
@AutoConfigureMockMvc
@DisplayName("core-test core-logging 요청 ID 통합 테스트")
class RequestIdLoggingIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("요청 헤더의 요청 ID를 응답 헤더/바디에 반영한다")
    fun usesIncomingRequestId() {
        val incomingId = "req-123"
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/logging/request-id")
                .header("X-Request-Id", incomingId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(header().string("X-Request-Id", incomingId))
            .andReturn()

        val body = result.response.contentAsString
        val mdcValue: String = JsonPath.read(body, "$.data.mdcRequestId")
        val headerValue: String = JsonPath.read(body, "$.data.headerRequestId")
        assertEquals(incomingId, mdcValue)
        assertEquals(incomingId, headerValue)
    }

    @Test
    @DisplayName("요청 ID가 없으면 생성해서 응답 헤더/바디에 반영한다")
    fun generatesRequestIdWhenMissing() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/logging/request-id")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(header().exists("X-Request-Id"))
            .andReturn()

        val headerId = result.response.getHeader("X-Request-Id")
        assertNotNull(headerId)
        assertFalse(headerId.isNullOrBlank())

        val body = result.response.contentAsString
        val mdcValue: String = JsonPath.read(body, "$.data.mdcRequestId")
        assertEquals(headerId, mdcValue)
    }
}
