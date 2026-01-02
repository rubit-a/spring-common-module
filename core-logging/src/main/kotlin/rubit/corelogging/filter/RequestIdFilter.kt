package rubit.corelogging.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import rubit.corelogging.config.RequestIdProperties

class RequestIdFilter(
    private val properties: RequestIdProperties
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val headerName = properties.header
        val incomingId = request.getHeader(headerName)
        val requestId = if (!incomingId.isNullOrBlank()) {
            incomingId
        } else if (properties.generateIfMissing) {
            UUID.randomUUID().toString()
        } else {
            null
        }

        if (requestId == null) {
            filterChain.doFilter(request, response)
            return
        }

        MDC.put(properties.mdcKey, requestId)
        response.setHeader(headerName, requestId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(properties.mdcKey)
        }
    }
}
