package rubit.coreexcel.template

interface ExcelTemplate<T : Any> {
    val templateId: String
    val sheetName: String

    fun headers(): List<String>

    fun mapRow(item: T): List<Any?>

    fun autoSizeColumns(): Boolean = false

    fun fileName(params: Map<String, String>): String? = null
}
