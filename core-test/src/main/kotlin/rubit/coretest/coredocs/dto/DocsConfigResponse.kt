package rubit.coretest.coredocs.dto

data class DocsConfigResponse(
    val title: String,
    val description: String?,
    val version: String?,
    val serverUrl: String?,
    val securityEnabled: Boolean,
    val schemeName: String,
    val scheme: String,
    val bearerFormat: String
)
