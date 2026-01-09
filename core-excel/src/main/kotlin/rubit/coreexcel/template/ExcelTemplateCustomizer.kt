package rubit.coreexcel.template

import rubit.coreexcel.util.ExcelWorkbookBuilder

interface ExcelTemplateCustomizer<T : Any> {
    fun render(builder: ExcelWorkbookBuilder, data: Iterable<T>)
}
