package rubit.coretest.coredocs.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rubit.coredocs.config.DocsProperties
import rubit.coretest.coredocs.dto.DocsConfigResponse

@RestController
@RequestMapping("/api/docs")
@ConditionalOnProperty(prefix = "core.docs", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class DocsConfigController(
    private val docsProperties: DocsProperties
) {

    @GetMapping("/config")
    fun getDocsConfig(): DocsConfigResponse {
        val security = docsProperties.security
        return DocsConfigResponse(
            title = docsProperties.title,
            description = docsProperties.description,
            version = docsProperties.version,
            serverUrl = docsProperties.serverUrl,
            securityEnabled = security.enabled,
            schemeName = security.schemeName,
            scheme = security.scheme,
            bearerFormat = security.bearerFormat
        )
    }
}
