package rubit.coreexcel.download

object ExcelFileName {
    fun ensureXlsx(fileName: String): String {
        return if (fileName.lowercase().endsWith(".xlsx")) {
            fileName
        } else {
            "$fileName.xlsx"
        }
    }
}
