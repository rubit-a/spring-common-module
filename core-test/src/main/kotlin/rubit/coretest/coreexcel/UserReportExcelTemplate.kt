package rubit.coretest.coreexcel

import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.springframework.stereotype.Component
import rubit.coreexcel.template.ExcelTemplate
import rubit.coreexcel.template.ExcelTemplateCustomizer
import rubit.coreexcel.util.ExcelCellWriter
import rubit.coreexcel.util.ExcelStyleFactory
import rubit.coreexcel.util.ExcelWorkbookBuilder
import rubit.coretest.coreauth.entity.UserEntity

@Component
class UserReportExcelTemplate(
    private val styleFactory: ExcelStyleFactory
) : ExcelTemplate<UserEntity>, ExcelTemplateCustomizer<UserEntity> {
    override val templateId: String = "users-report"
    override val sheetName: String = "User Report"

    override fun headers(): List<String> {
        return listOf("Username", "Status", "Roles", "Role Count")
    }

    override fun mapRow(item: UserEntity): List<Any?> {
        return listOf(
            item.username,
            if (item.enabled) "ENABLED" else "DISABLED",
            item.authorities.sorted().joinToString(", "),
            item.authorities.size
        )
    }

    override fun render(builder: ExcelWorkbookBuilder, data: Iterable<UserEntity>) {
        val users = data.toList()
        val workbook = builder.build()
        val sheet = workbook.createSheet(sheetName)
        val styles = styleFactory.create(workbook)
        val writer = ExcelCellWriter(styles)
        val dataFormatHelper = workbook.creationHelper.createDataFormat()

        val titleFont = workbook.createFont().apply {
            bold = true
            fontHeightInPoints = 16
        }
        val titleStyle = workbook.createCellStyle().apply {
            setFont(titleFont)
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        val sectionFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }
        val sectionStyle = workbook.createCellStyle().apply {
            setFont(sectionFont)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        val metaLabelStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }
        val metaValueStyle = workbook.createCellStyle().apply {
            this.dataFormat = dataFormatHelper.getFormat("yyyy-MM-dd HH:mm")
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
        }

        val headerFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }
        val tableHeaderStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
            fillForegroundColor = IndexedColors.ROYAL_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val numberFormat = dataFormatHelper.getFormat("0")
        fun createBodyStyle(
            fill: IndexedColors? = null,
            format: Short? = null,
            font: Font? = null
        ): CellStyle {
            return workbook.createCellStyle().apply {
                alignment = HorizontalAlignment.LEFT
                verticalAlignment = VerticalAlignment.CENTER
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
                if (fill != null) {
                    fillForegroundColor = fill.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                }
                if (format != null) {
                    this.dataFormat = format
                }
                if (font != null) {
                    setFont(font)
                }
            }
        }

        val summaryLabelFont = workbook.createFont().apply { bold = true }
        val summaryLabelStyle = createBodyStyle(font = summaryLabelFont)
        val summaryNumberStyle = createBodyStyle(format = numberFormat)

        val bodyStyle = createBodyStyle()
        val bodyNumberStyle = createBodyStyle(format = numberFormat)
        val bodyAltStyle = createBodyStyle(fill = IndexedColors.GREY_25_PERCENT)
        val bodyAltNumberStyle = createBodyStyle(fill = IndexedColors.GREY_25_PERCENT, format = numberFormat)
        val disabledStyle = createBodyStyle(fill = IndexedColors.ROSE)
        val disabledNumberStyle = createBodyStyle(fill = IndexedColors.ROSE, format = numberFormat)

        var rowIndex = 0
        fun nextRow(height: Float? = null): Row {
            val row = sheet.createRow(rowIndex++)
            if (height != null) {
                row.heightInPoints = height
            }
            return row
        }

        fun writeCell(row: Row, column: Int, value: Any?, style: CellStyle? = null) {
            val cell = row.createCell(column)
            writer.write(cell, value, style)
        }

        val headerTitles = headers()
        val lastColumn = headerTitles.lastIndex

        val titleRow = nextRow(28f)
        writeCell(titleRow, 0, "User Access Report", titleStyle)
        sheet.addMergedRegion(CellRangeAddress(titleRow.rowNum, titleRow.rowNum, 0, lastColumn))

        val metaRow = nextRow()
        writeCell(metaRow, 0, "Generated at", metaLabelStyle)
        writeCell(metaRow, 1, LocalDateTime.now(), metaValueStyle)

        nextRow()

        val summaryHeaderRow = nextRow()
        writeCell(summaryHeaderRow, 0, "Summary", sectionStyle)
        sheet.addMergedRegion(
            CellRangeAddress(summaryHeaderRow.rowNum, summaryHeaderRow.rowNum, 0, lastColumn)
        )

        val totalUsers = users.size
        val enabledUsers = users.count { it.enabled }
        val disabledUsers = totalUsers - enabledUsers

        val totalRow = nextRow()
        writeCell(totalRow, 0, "Total users", summaryLabelStyle)
        writeCell(totalRow, 1, totalUsers, summaryNumberStyle)

        val enabledRow = nextRow()
        writeCell(enabledRow, 0, "Enabled users", summaryLabelStyle)
        writeCell(enabledRow, 1, enabledUsers, summaryNumberStyle)

        val disabledRow = nextRow()
        writeCell(disabledRow, 0, "Disabled users", summaryLabelStyle)
        writeCell(disabledRow, 1, disabledUsers, summaryNumberStyle)

        nextRow()

        val headerRow = nextRow(20f)
        headerTitles.forEachIndexed { index, title ->
            writeCell(headerRow, index, title, tableHeaderStyle)
        }

        val headerRowIndex = headerRow.rowNum
        users.forEachIndexed { index, user ->
            val row = nextRow()
            val rowStyle = when {
                !user.enabled -> disabledStyle
                index % 2 == 0 -> bodyAltStyle
                else -> bodyStyle
            }
            val numberStyle = when {
                !user.enabled -> disabledNumberStyle
                index % 2 == 0 -> bodyAltNumberStyle
                else -> bodyNumberStyle
            }

            writeCell(row, 0, user.username, rowStyle)
            writeCell(row, 1, if (user.enabled) "ENABLED" else "DISABLED", rowStyle)
            writeCell(row, 2, user.authorities.sorted().joinToString(", "), rowStyle)
            writeCell(row, 3, user.authorities.size, numberStyle)
        }

        val lastRowIndex = if (users.isEmpty()) headerRowIndex else rowIndex - 1
        sheet.createFreezePane(0, headerRowIndex + 1)
        sheet.setAutoFilter(CellRangeAddress(headerRowIndex, lastRowIndex, 0, lastColumn))

        sheet.setColumnWidth(0, 24 * 256)
        sheet.setColumnWidth(1, 14 * 256)
        sheet.setColumnWidth(2, 48 * 256)
        sheet.setColumnWidth(3, 12 * 256)
    }
}
