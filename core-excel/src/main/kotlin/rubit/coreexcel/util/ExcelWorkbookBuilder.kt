package rubit.coreexcel.util

import java.io.ByteArrayOutputStream
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelWorkbookBuilder(
    private val styleFactory: ExcelStyleFactory,
    private val workbook: Workbook = XSSFWorkbook()
) {
    private val styles = styleFactory.create(workbook)

    fun sheet(name: String, block: ExcelSheetBuilder.() -> Unit): ExcelWorkbookBuilder {
        val sheet = workbook.createSheet(name)
        val builder = ExcelSheetBuilder(sheet, styles)
        builder.block()
        return this
    }

    fun build(): Workbook = workbook

    fun toByteArray(closeWorkbook: Boolean = true): ByteArray {
        return ByteArrayOutputStream().use { output ->
            workbook.write(output)
            if (closeWorkbook) {
                workbook.close()
            }
            output.toByteArray()
        }
    }
}
