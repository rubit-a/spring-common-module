package rubit.corelogging.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import rubit.corelogging.config.TraceProperties

class TraceContextFilter(
    private val properties: TraceProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val traceContext = resolveTraceContext(request)
        if (traceContext == null) {
            filterChain.doFilter(request, response)
            return
        }

        MDC.put(properties.mdcTraceIdKey, traceContext.traceId)
        MDC.put(properties.mdcSpanIdKey, traceContext.spanId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(properties.mdcTraceIdKey)
            MDC.remove(properties.mdcSpanIdKey)
        }
    }

    private fun resolveTraceContext(request: HttpServletRequest): TraceContext? {
        val traceparentHeader = request.getHeader(TRACEPARENT_HEADER)
        if (!traceparentHeader.isNullOrBlank()) {
            parseTraceparent(traceparentHeader)?.let { return it }
        }

        val b3Header = request.getHeader(B3_HEADER)
        if (!b3Header.isNullOrBlank()) {
            parseB3Single(b3Header)?.let { return it }
        }

        val b3TraceId = request.getHeader(B3_TRACE_ID_HEADER)
        val b3SpanId = request.getHeader(B3_SPAN_ID_HEADER)
        if (!b3TraceId.isNullOrBlank() && !b3SpanId.isNullOrBlank()) {
            parseB3Multi(b3TraceId, b3SpanId)?.let { return it }
        }

        if (!properties.generateIfMissing) {
            return null
        }

        return TraceContext(
            traceId = generateTraceId(),
            spanId = generateSpanId()
        )
    }

    private fun parseTraceparent(header: String): TraceContext? {
        val match = TRACEPARENT_PATTERN.matchEntire(header.trim()) ?: return null
        val traceId = match.groupValues[2].lowercase()
        val spanId = match.groupValues[3].lowercase()
        if (!isValidTraceId(traceId, expectedLength = 32) || !isValidSpanId(spanId)) {
            return null
        }
        return TraceContext(traceId, spanId)
    }

    private fun parseB3Single(header: String): TraceContext? {
        val match = B3_SINGLE_PATTERN.matchEntire(header.trim()) ?: return null
        val traceId = match.groupValues[1].lowercase()
        val spanId = match.groupValues[2].lowercase()
        if (!isValidTraceId(traceId) || !isValidSpanId(spanId)) {
            return null
        }
        return TraceContext(traceId, spanId)
    }

    private fun parseB3Multi(traceIdHeader: String, spanIdHeader: String): TraceContext? {
        val traceId = traceIdHeader.trim().lowercase()
        val spanId = spanIdHeader.trim().lowercase()
        if (!isValidTraceId(traceId) || !isValidSpanId(spanId)) {
            return null
        }
        return TraceContext(traceId, spanId)
    }

    private fun isValidTraceId(value: String, expectedLength: Int? = null): Boolean {
        if (expectedLength != null && value.length != expectedLength) {
            return false
        }
        if (value.length != 16 && value.length != 32) {
            return false
        }
        if (!HEX_PATTERN.matches(value)) {
            return false
        }
        if (value.all { it == '0' }) {
            return false
        }
        return true
    }

    private fun isValidSpanId(value: String): Boolean {
        if (value.length != 16) {
            return false
        }
        if (!HEX_PATTERN.matches(value)) {
            return false
        }
        if (value.all { it == '0' }) {
            return false
        }
        return true
    }

    private fun generateTraceId(): String =
        UUID.randomUUID().toString().replace("-", "").lowercase()

    private fun generateSpanId(): String {
        val hexValue = java.lang.Long.toHexString(ThreadLocalRandom.current().nextLong())
        val padded = hexValue.padStart(16, '0')
        return if (padded.all { it == '0' }) {
            generateSpanId()
        } else {
            padded.lowercase()
        }
    }

    private data class TraceContext(
        val traceId: String,
        val spanId: String
    )

    private companion object {
        private const val TRACEPARENT_HEADER = "traceparent"
        private const val B3_HEADER = "b3"
        private const val B3_TRACE_ID_HEADER = "X-B3-TraceId"
        private const val B3_SPAN_ID_HEADER = "X-B3-SpanId"

        private val TRACEPARENT_PATTERN = Regex(
            "^([0-9a-f]{2})-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})$",
            RegexOption.IGNORE_CASE
        )
        private val B3_SINGLE_PATTERN = Regex(
            "^([0-9a-f]{16}|[0-9a-f]{32})-([0-9a-f]{16})(?:-[01d])?(?:-[0-9a-f]{16})?$",
            RegexOption.IGNORE_CASE
        )
        private val HEX_PATTERN = Regex("^[0-9a-f]+$", RegexOption.IGNORE_CASE)
    }
}
