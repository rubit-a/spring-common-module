package rubit.coreexcel.util

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.RichTextString

class ExcelCellWriter(
    private val styles: ExcelStyles
) {
    fun write(cell: Cell, value: Any?, style: CellStyle? = null) {
        if (value == null) {
            if (style != null) {
                cell.cellStyle = style
            }
            return
        }

        var resolvedStyle = style

        when (value) {
            is String -> cell.setCellValue(value)
            is RichTextString -> cell.setCellValue(value)
            is Boolean -> cell.setCellValue(value)
            is Int -> cell.setCellValue(value.toDouble())
            is Long -> cell.setCellValue(value.toDouble())
            is Short -> cell.setCellValue(value.toDouble())
            is Double -> cell.setCellValue(value)
            is Float -> cell.setCellValue(value.toDouble())
            is BigDecimal -> cell.setCellValue(value.toDouble())
            is LocalDate -> {
                cell.setCellValue(value)
                if (resolvedStyle == null) {
                    resolvedStyle = styles.date
                }
            }
            is LocalDateTime -> {
                cell.setCellValue(value)
                if (resolvedStyle == null) {
                    resolvedStyle = styles.dateTime
                }
            }
            is LocalTime -> {
                cell.setCellValue(value.atDate(LocalDate.of(1899, 12, 31)))
                if (resolvedStyle == null) {
                    resolvedStyle = styles.time
                }
            }
            is Date -> {
                cell.setCellValue(value)
                if (resolvedStyle == null) {
                    resolvedStyle = styles.dateTime
                }
            }
            is Instant -> {
                cell.setCellValue(Date.from(value))
                if (resolvedStyle == null) {
                    resolvedStyle = styles.dateTime
                }
            }
            is Enum<*> -> cell.setCellValue(value.name)
            else -> cell.setCellValue(value.toString())
        }

        if (resolvedStyle != null) {
            cell.cellStyle = resolvedStyle
        }
    }
}
