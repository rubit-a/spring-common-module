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
import rubit.corelogging.config.TraceProperties

class TraceContextFilterTest {

    @AfterEach
    fun clearMdc() {
        MDC.clear()
    }

    @Test
    @DisplayName("traceparent 헤더의 traceId/spanId를 MDC에 주입한다")
    fun `uses traceparent header`() {
        val properties = TraceProperties(
            enabled = true,
            mdcTraceIdKey = "traceId",
            mdcSpanIdKey = "spanId",
            generateIfMissing = true
        )
        val filter = TraceContextFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val traceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
        request.addHeader("traceparent", traceparent)
        val chain = CapturingFilterChain(properties.mdcTraceIdKey, properties.mdcSpanIdKey)

        filter.doFilter(request, response, chain)

        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", chain.traceId)
        assertEquals("00f067aa0ba902b7", chain.spanId)
        assertNull(MDC.get(properties.mdcTraceIdKey))
        assertNull(MDC.get(properties.mdcSpanIdKey))
    }

    @Test
    @DisplayName("b3 단일 헤더의 traceId/spanId를 MDC에 주입한다")
    fun `uses b3 single header`() {
        val properties = TraceProperties(
            enabled = true,
            mdcTraceIdKey = "traceId",
            mdcSpanIdKey = "spanId",
            generateIfMissing = true
        )
        val filter = TraceContextFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val b3 = "80f198ee56343ba864fe8b2a57d3eff7-05e3ac9a4f6e3b90-1"
        request.addHeader("b3", b3)
        val chain = CapturingFilterChain(properties.mdcTraceIdKey, properties.mdcSpanIdKey)

        filter.doFilter(request, response, chain)

        assertEquals("80f198ee56343ba864fe8b2a57d3eff7", chain.traceId)
        assertEquals("05e3ac9a4f6e3b90", chain.spanId)
        assertNull(MDC.get(properties.mdcTraceIdKey))
        assertNull(MDC.get(properties.mdcSpanIdKey))
    }

    @Test
    @DisplayName("헤더가 없으면 traceId/spanId를 생성한다")
    fun `generates trace context when missing`() {
        val properties = TraceProperties(
            enabled = true,
            mdcTraceIdKey = "traceId",
            mdcSpanIdKey = "spanId",
            generateIfMissing = true
        )
        val filter = TraceContextFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain(properties.mdcTraceIdKey, properties.mdcSpanIdKey)

        filter.doFilter(request, response, chain)

        assertNotNull(chain.traceId)
        assertNotNull(chain.spanId)
        assertTrue(chain.traceId!!.length == 32)
        assertTrue(chain.spanId!!.length == 16)
        assertNull(MDC.get(properties.mdcTraceIdKey))
        assertNull(MDC.get(properties.mdcSpanIdKey))
    }

    @Test
    @DisplayName("헤더가 없고 생성 비활성이면 MDC를 설정하지 않는다")
    fun `skips trace context when missing and disabled`() {
        val properties = TraceProperties(
            enabled = true,
            mdcTraceIdKey = "traceId",
            mdcSpanIdKey = "spanId",
            generateIfMissing = false
        )
        val filter = TraceContextFilter(properties)
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = CapturingFilterChain(properties.mdcTraceIdKey, properties.mdcSpanIdKey)

        filter.doFilter(request, response, chain)

        assertNull(chain.traceId)
        assertNull(chain.spanId)
        assertNull(MDC.get(properties.mdcTraceIdKey))
        assertNull(MDC.get(properties.mdcSpanIdKey))
    }

    private class CapturingFilterChain(
        private val traceIdKey: String,
        private val spanIdKey: String
    ) : FilterChain {
        var traceId: String? = null
        var spanId: String? = null

        override fun doFilter(request: ServletRequest, response: ServletResponse) {
            traceId = MDC.get(traceIdKey)
            spanId = MDC.get(spanIdKey)
        }
    }
}
