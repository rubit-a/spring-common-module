package rubit.coreexcel.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "core.excel")
data class CoreExcelProperties(
    val download: DownloadProperties = DownloadProperties(),
    val style: StyleProperties = StyleProperties()
)

data class DownloadProperties(
    val enabled: Boolean = false,
    val path: String = "/api/excel",
    val defaultFileName: String = "export.xlsx"
)

data class StyleProperties(
    val date: String = "yyyy-MM-dd",
    val dateTime: String = "yyyy-MM-dd HH:mm:ss",
    val time: String = "HH:mm:ss",
    val headerBold: Boolean = true
)
