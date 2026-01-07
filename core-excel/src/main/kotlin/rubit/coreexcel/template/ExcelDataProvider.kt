package rubit.coreexcel.template

interface ExcelDataProvider<T : Any> {
    val templateId: String

    fun fetch(params: Map<String, String>): List<T>

    fun fileName(params: Map<String, String>): String? = null
}
