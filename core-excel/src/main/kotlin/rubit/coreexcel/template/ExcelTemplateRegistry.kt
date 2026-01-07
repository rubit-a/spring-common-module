package rubit.coreexcel.template

class ExcelTemplateRegistry(templates: List<ExcelTemplate<*>>) {
    private val templateMap = templates.associateBy { it.templateId }

    init {
        val duplicates = templates.groupBy { it.templateId }
            .filterValues { it.size > 1 }
            .keys
        require(duplicates.isEmpty()) {
            "Duplicate excel templateId(s): ${duplicates.joinToString(", ")}"
        }
    }

    fun find(templateId: String): ExcelTemplate<*>? = templateMap[templateId]
}
