package rubit.coreexcel.download

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import rubit.coreweb.response.ApiError
import rubit.coreweb.response.ApiResponse
import rubit.coreweb.response.ErrorCode

@RestControllerAdvice
class ExcelDownloadApiErrorHandler {

    @ExceptionHandler(ExcelTemplateNotFoundException::class)
    fun handleTemplateNotFound(
        ex: ExcelTemplateNotFoundException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf("templateId" to ex.templateId)
        return errorResponse(ErrorCode.NOT_FOUND, ex.message, details)
    }

    @ExceptionHandler(ExcelDataProviderNotFoundException::class)
    fun handleDataProviderNotFound(
        ex: ExcelDataProviderNotFoundException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf("templateId" to ex.templateId)
        return errorResponse(ErrorCode.INTERNAL_ERROR, ex.message, details)
    }

    private fun errorResponse(
        errorCode: ErrorCode,
        message: String?,
        details: Map<String, Any?>? = null
    ): ResponseEntity<ApiResponse<Nothing>> {
        val normalizedMessage = message?.takeIf { it.isNotBlank() }
        val apiError = ApiError.from(errorCode, normalizedMessage, details)
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(apiError))
    }
}
