package rubit.coreexcel.template

import rubit.coreexcel.util.ExcelWorkbookBuilderFactory

class ExcelTemplateRenderer(
    private val workbookBuilderFactory: ExcelWorkbookBuilderFactory
) {
    fun <T : Any> render(template: ExcelTemplate<T>, data: Iterable<T>): ByteArray {
        val builder = workbookBuilderFactory.create()
        builder.sheet(template.sheetName) {
            val headers = template.headers()
            if (headers.isNotEmpty()) {
                header(headers)
            }

            data.forEach { item ->
                row(template.mapRow(item))
            }

            if (template.autoSizeColumns()) {
                autoSizeColumns()
            }
        }
        return builder.toByteArray()
    }
}
