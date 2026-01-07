package rubit.coreexcel.download

data class ExcelDownloadRequest(
    val templateId: String,
    val params: Map<String, String>
)
