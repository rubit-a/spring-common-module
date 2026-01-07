package rubit.coreexcel.template

class ExcelDataProviderRegistry(providers: List<ExcelDataProvider<*>>) {
    private val providerMap = providers.associateBy { it.templateId }

    init {
        val duplicates = providers.groupBy { it.templateId }
            .filterValues { it.size > 1 }
            .keys
        require(duplicates.isEmpty()) {
            "Duplicate excel data provider templateId(s): ${duplicates.joinToString(", ")}"
        }
    }

    fun find(templateId: String): ExcelDataProvider<*>? = providerMap[templateId]
}
