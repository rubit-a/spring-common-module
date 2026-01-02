package rubit.coretest.corelogging.controller

import org.slf4j.MDC
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/logging")
class LoggingSampleController {

    @GetMapping("/request-id")
    fun requestId(
        @RequestHeader(name = "X-Request-Id", required = false)
        headerRequestId: String?
    ): Map<String, String?> {
        logger.info("request-id check header={}, mdc={}", headerRequestId, MDC.get("requestId"))
        return mapOf(
            "mdcRequestId" to MDC.get("requestId"),
            "headerRequestId" to headerRequestId
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingSampleController::class.java)
    }
}
