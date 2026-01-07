package rubit.coreexcel.download

import rubit.coreexcel.config.CoreExcelProperties
import rubit.coreexcel.template.ExcelDataProvider
import rubit.coreexcel.template.ExcelDataProviderRegistry
import rubit.coreexcel.template.ExcelTemplate
import rubit.coreexcel.template.ExcelTemplateRegistry
import rubit.coreexcel.template.ExcelTemplateRenderer

class DefaultExcelDownloadService(
    private val templateRegistry: ExcelTemplateRegistry,
    private val dataProviderRegistry: ExcelDataProviderRegistry,
    private val renderer: ExcelTemplateRenderer,
    private val properties: CoreExcelProperties
) : ExcelDownloadService {
    override fun download(request: ExcelDownloadRequest): ExcelDownloadResult {
        val template = templateRegistry.find(request.templateId)
            ?: throw ExcelTemplateNotFoundException(request.templateId)
        val provider = dataProviderRegistry.find(request.templateId)
            ?: throw ExcelDataProviderNotFoundException(request.templateId)

        val content = render(template, provider, request.params)
        val fileName = resolveFileName(template, provider, request.params)

        return ExcelDownloadResult(fileName, content)
    }

    private fun resolveFileName(
        template: ExcelTemplate<*>,
        provider: ExcelDataProvider<*>,
        params: Map<String, String>
    ): String {
        val candidate = provider.fileName(params)
            ?: template.fileName(params)
            ?: properties.download.defaultFileName
        return ExcelFileName.ensureXlsx(candidate)
    }

    @Suppress("UNCHECKED_CAST")
    private fun render(
        template: ExcelTemplate<*>,
        provider: ExcelDataProvider<*>,
        params: Map<String, String>
    ): ByteArray {
        val typedTemplate = template as ExcelTemplate<Any>
        val typedProvider = provider as ExcelDataProvider<Any>
        val data = typedProvider.fetch(params)
        return renderer.render(typedTemplate, data)
    }
}
