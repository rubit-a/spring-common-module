package rubit.coreexcel.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import rubit.coreexcel.download.DefaultExcelDownloadService
import rubit.coreexcel.download.ExcelDownloadApiErrorHandler
import rubit.coreexcel.download.ExcelDownloadController
import rubit.coreexcel.download.ExcelDownloadService
import rubit.coreexcel.template.ExcelDataProvider
import rubit.coreexcel.template.ExcelDataProviderRegistry
import rubit.coreexcel.template.ExcelTemplate
import rubit.coreexcel.template.ExcelTemplateRegistry
import rubit.coreexcel.template.ExcelTemplateRenderer
import rubit.coreexcel.util.ExcelStyleFactory
import rubit.coreexcel.util.ExcelWorkbookBuilderFactory

@AutoConfiguration
@EnableConfigurationProperties(CoreExcelProperties::class)
class CoreExcelAutoConfiguration {
    @Bean
    fun excelStyleFactory(properties: CoreExcelProperties): ExcelStyleFactory {
        return ExcelStyleFactory(properties.style)
    }

    @Bean
    fun excelWorkbookBuilderFactory(styleFactory: ExcelStyleFactory): ExcelWorkbookBuilderFactory {
        return ExcelWorkbookBuilderFactory(styleFactory)
    }

    @Bean
    fun excelTemplateRenderer(builderFactory: ExcelWorkbookBuilderFactory): ExcelTemplateRenderer {
        return ExcelTemplateRenderer(builderFactory)
    }

    @Bean
    fun excelTemplateRegistry(templates: List<ExcelTemplate<*>>): ExcelTemplateRegistry {
        return ExcelTemplateRegistry(templates)
    }

    @Bean
    fun excelDataProviderRegistry(providers: List<ExcelDataProvider<*>>): ExcelDataProviderRegistry {
        return ExcelDataProviderRegistry(providers)
    }

    @Bean
    @ConditionalOnMissingBean
    fun excelDownloadService(
        templateRegistry: ExcelTemplateRegistry,
        dataProviderRegistry: ExcelDataProviderRegistry,
        renderer: ExcelTemplateRenderer,
        properties: CoreExcelProperties
    ): ExcelDownloadService {
        return DefaultExcelDownloadService(templateRegistry, dataProviderRegistry, renderer, properties)
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(
        prefix = "core.excel.download",
        name = ["enabled"],
        havingValue = "true"
    )
    fun excelDownloadController(excelDownloadService: ExcelDownloadService): ExcelDownloadController {
        return ExcelDownloadController(excelDownloadService)
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(
        prefix = "core.excel.download",
        name = ["enabled"],
        havingValue = "true"
    )
    @ConditionalOnProperty(
        prefix = "core.web.response",
        name = ["enabled"],
        havingValue = "true"
    )
    @ConditionalOnClass(name = ["rubit.coreweb.response.ApiResponse"])
    @ConditionalOnMissingBean(ExcelDownloadApiErrorHandler::class)
    fun excelDownloadApiErrorHandler(): ExcelDownloadApiErrorHandler {
        return ExcelDownloadApiErrorHandler()
    }
}
