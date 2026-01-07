package rubit.coreexcel.util

import kotlin.math.max
import kotlin.math.min
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Sheet

class ExcelSheetBuilder(
    private val sheet: Sheet,
    private val styles: ExcelStyles
) {
    private val cellWriter = ExcelCellWriter(styles)
    private var rowIndex = 0
    private var maxColumnCount = 0

    fun header(values: List<String>, style: CellStyle = styles.header) {
        row(values, style)
    }

    fun row(values: List<Any?>, style: CellStyle? = null) {
        val row = sheet.createRow(rowIndex++)
        values.forEachIndexed { index, value ->
            val cell = row.createCell(index)
            cellWriter.write(cell, value, style)
        }
        maxColumnCount = max(maxColumnCount, values.size)
    }

    fun row(vararg values: Any?) {
        row(values.toList())
    }

    fun rows(rows: Iterable<List<Any?>>, style: CellStyle? = null) {
        rows.forEach { row(it, style) }
    }

    fun autoSizeColumns(from: Int = 0, to: Int = maxColumnCount - 1) {
        if (maxColumnCount == 0) {
            return
        }

        val lastIndex = min(to, maxColumnCount - 1)
        if (from > lastIndex) {
            return
        }

        for (index in from..lastIndex) {
            sheet.autoSizeColumn(index)
        }
    }
}
