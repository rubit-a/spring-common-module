package rubit.coreexcel.util

class ExcelWorkbookBuilderFactory(
    private val styleFactory: ExcelStyleFactory
) {
    fun create(): ExcelWorkbookBuilder = ExcelWorkbookBuilder(styleFactory)
}
