package rubit.coreexcel.download

class ExcelTemplateNotFoundException(
    val templateId: String
) : RuntimeException(
    "Excel template not found: $templateId"
)

class ExcelDataProviderNotFoundException(
    val templateId: String
) : RuntimeException(
    "Excel data provider not found: $templateId"
)
