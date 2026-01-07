package rubit.coreexcel.download

data class ExcelDownloadResult(
    val fileName: String,
    val content: ByteArray
)
