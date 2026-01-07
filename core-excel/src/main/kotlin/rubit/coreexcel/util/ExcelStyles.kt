package rubit.coreexcel.util

import org.apache.poi.ss.usermodel.CellStyle

data class ExcelStyles(
    val header: CellStyle,
    val date: CellStyle,
    val dateTime: CellStyle,
    val time: CellStyle
)
