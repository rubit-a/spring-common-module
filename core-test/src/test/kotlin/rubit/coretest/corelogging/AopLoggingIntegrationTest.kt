package rubit.coretest.corelogging

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import rubit.coretest.CoreTestApplication

@SpringBootTest(
    classes = [CoreTestApplication::class],
    properties = [
        "core.logging.aop.enabled=true",
        "core.logging.aop.log-args=true",
        "core.logging.aop.log-result=true"
    ]
)
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension::class)
@DisplayName("core-test core-logging AOP 통합 테스트")
class AopLoggingIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("AOP 실행시간 로그와 샘플 로그가 기록된다")
    fun logsAopAndSample(output: CapturedOutput) {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/public/logging/aop")
                .param("message", "hello")
        )
            .andExpect(status().isOk)

        val content = output.toString()
        assertTrue(content.contains("aop elapsedMs="))
        assertTrue(content.contains("LoggingAopSampleService.work"))
        assertTrue(content.contains("aop sample message=hello"))
    }
}
