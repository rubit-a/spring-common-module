package rubit.coreexcel.util

import org.apache.poi.ss.usermodel.Workbook
import rubit.coreexcel.config.StyleProperties

class ExcelStyleFactory(
    private val properties: StyleProperties
) {
    fun create(workbook: Workbook): ExcelStyles {
        val dataFormat = workbook.creationHelper.createDataFormat()

        val headerFont = workbook.createFont().apply {
            bold = properties.headerBold
        }

        val headerStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
        }

        val dateStyle = workbook.createCellStyle().apply {
            this.dataFormat = dataFormat.getFormat(properties.date)
        }

        val dateTimeStyle = workbook.createCellStyle().apply {
            this.dataFormat = dataFormat.getFormat(properties.dateTime)
        }

        val timeStyle = workbook.createCellStyle().apply {
            this.dataFormat = dataFormat.getFormat(properties.time)
        }

        return ExcelStyles(
            header = headerStyle,
            date = dateStyle,
            dateTime = dateTimeStyle,
            time = timeStyle
        )
    }
}
