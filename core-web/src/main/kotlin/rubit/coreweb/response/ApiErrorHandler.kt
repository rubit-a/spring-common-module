package rubit.coreweb.response

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class ApiErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiResponse<Nothing>> {
        return validationErrorResponse(ex.bindingResult)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException
    ): ResponseEntity<ApiResponse<Nothing>> {
        return validationErrorResponse(ex.bindingResult)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val violations = ex.constraintViolations
            .groupBy { it.propertyPath.toString() }
            .mapValues { entry -> entry.value.map { it.message } }
        val details = mapOf("violations" to violations)
        return errorResponse(ErrorCode.VALIDATION_ERROR, details = details)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf(
            "parameter" to ex.parameterName,
            "type" to ex.parameterType
        )
        return errorResponse(ErrorCode.MISSING_PARAMETER, details = details)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf(
            "parameter" to ex.name,
            "value" to ex.value,
            "requiredType" to ex.requiredType?.simpleName
        )
        return errorResponse(ErrorCode.TYPE_MISMATCH, details = details)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf(
            "method" to ex.method,
            "supported" to ex.supportedMethods?.toList()
        )
        return errorResponse(
            ErrorCode.METHOD_NOT_ALLOWED,
            message = ex.message,
            details = details
        )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf(
            "contentType" to ex.contentType?.toString(),
            "supported" to ex.supportedMediaTypes.map { it.toString() }
        )
        return errorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, details = details)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFound(
        ex: NoHandlerFoundException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val details = mapOf(
            "path" to ex.requestURL,
            "method" to ex.httpMethod
        )
        return errorResponse(ErrorCode.NOT_FOUND, details = details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.mostSpecificCause?.message ?: ErrorCode.MALFORMED_REQUEST.defaultMessage
        return errorResponse(ErrorCode.MALFORMED_REQUEST, message = message)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val status = ex.statusCode
        val mapped = ErrorCode.fromStatus(status)
        val message = ex.reason ?: mapped?.defaultMessage ?: "Request failed"
        return errorResponse(status, message)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.message ?: ErrorCode.INVALID_REQUEST.defaultMessage
        return errorResponse(ErrorCode.INVALID_REQUEST, message = message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception
    ): ResponseEntity<ApiResponse<Nothing>> {
        return errorResponse(ErrorCode.INTERNAL_ERROR)
    }

    private fun validationErrorResponse(
        bindingResult: BindingResult
    ): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors = bindingResult.fieldErrors
            .groupBy { it.field }
            .mapValues { entry ->
                entry.value.map { it.defaultMessage ?: "Invalid value" }
            }
        val globalErrors = bindingResult.globalErrors.map { it.defaultMessage ?: it.objectName }
        val details = mutableMapOf<String, Any?>(
            "fields" to fieldErrors
        )
        if (globalErrors.isNotEmpty()) {
            details["global"] = globalErrors
        }
        return errorResponse(ErrorCode.VALIDATION_ERROR, details = details)
    }

    private fun errorResponse(
        errorCode: ErrorCode,
        message: String? = null,
        details: Map<String, Any?>? = null
    ): ResponseEntity<ApiResponse<Nothing>> {
        val normalizedMessage = message?.takeIf { it.isNotBlank() }
        val apiError = ApiError.from(errorCode, normalizedMessage, details)
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(apiError))
    }

    private fun errorResponse(
        status: HttpStatusCode,
        message: String,
        details: Map<String, Any?>? = null
    ): ResponseEntity<ApiResponse<Nothing>> {
        val mapped = ErrorCode.fromStatus(status)
        val apiError = if (mapped != null) {
            ApiError.from(mapped, message, details)
        } else {
            ApiError(statusCodeLabel(status), message, details)
        }
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(apiError))
    }

    private fun statusCodeLabel(status: HttpStatusCode): String {
        return if (status is HttpStatus) {
            status.name
        } else {
            "HTTP_${status.value()}"
        }
    }
}
