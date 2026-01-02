package rubit.coretest.corelogging.controller

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import rubit.coretest.corelogging.service.LoggingAopSampleService

@RestController
@RequestMapping("/api/public/logging")
class LoggingSampleController(
    private val aopSampleService: LoggingAopSampleService
) {

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

    @GetMapping("/aop")
    fun aop(
        @RequestParam(name = "message", defaultValue = "hello")
        message: String
    ): Map<String, String> {
        val result = aopSampleService.work(message)
        logger.info("aop sample message={} result={}", message, result)
        return mapOf("result" to result)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingSampleController::class.java)
    }
}
