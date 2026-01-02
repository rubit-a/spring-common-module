package rubit.corelogging.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import rubit.corelogging.config.RequestIdProperties

class RequestIdFilterTest {

    @AfterEach
    fun clearMdc() {
        MDC.clear()
    }

    @Test
    @DisplayName("요청 헤더의 Request ID를 그대로 사용한다")
    fun `uses incoming request id header`() {
        val properties = RequestIdProperties(
            enabled = true,
            header = "X-Request-Id",
            mdcKey = "requestId",
            generateIfMissing = true
        )
        val filter = RequestIdFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val incomingId = "abc-123"
        request.addHeader(properties.header, incomingId)
        val chain = CapturingFilterChain(properties.mdcKey)

        filter.doFilter(request, response, chain)

        assertEquals(incomingId, chain.mdcValue)
        assertEquals(incomingId, response.getHeader(properties.header))
        assertNull(MDC.get(properties.mdcKey))
    }

    @Test
    @DisplayName("요청 헤더가 없으면 Request ID를 생성한다")
    fun `generates request id when missing and enabled`() {
        val properties = RequestIdProperties(
            enabled = true,
            header = "X-Request-Id",
            mdcKey = "requestId",
            generateIfMissing = true
        )
        val filter = RequestIdFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain(properties.mdcKey)

        filter.doFilter(request, response, chain)

        val responseId = response.getHeader(properties.header)
        assertNotNull(responseId)
        assertTrue(responseId!!.isNotBlank())
        assertEquals(responseId, chain.mdcValue)
        assertNull(MDC.get(properties.mdcKey))
    }

    @Test
    @DisplayName("요청 헤더가 없고 생성 비활성이면 Request ID를 설정하지 않는다")
    fun `skips request id when missing and disabled`() {
        val properties = RequestIdProperties(
            enabled = true,
            header = "X-Request-Id",
            mdcKey = "requestId",
            generateIfMissing = false
        )
        val filter = RequestIdFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain(properties.mdcKey)

        filter.doFilter(request, response, chain)

        assertNull(chain.mdcValue)
        assertNull(response.getHeader(properties.header))
        assertNull(MDC.get(properties.mdcKey))
    }

    private class CapturingFilterChain(
        private val mdcKey: String
    ) : FilterChain {
        var mdcValue: String? = null

        override fun doFilter(request: ServletRequest, response: ServletResponse) {
            mdcValue = MDC.get(mdcKey)
        }
    }
}
