package rubit.coreweb.response

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

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
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ApiError("VALIDATION_ERROR", "Validation failed", details)))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.mostSpecificCause?.message ?: "Malformed request body"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ApiError("INVALID_REQUEST", message)))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val status = ex.statusCode
        val message = ex.reason ?: "Request failed"
        val code = statusCodeLabel(status)
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(ApiError(code, message)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ApiResponse<Nothing>> {
        val message = ex.message ?: "Invalid request"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ApiError("INVALID_REQUEST", message)))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception
    ): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ApiError("INTERNAL_ERROR", "Internal server error")))
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
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ApiError("VALIDATION_ERROR", "Validation failed", details)))
    }

    private fun statusCodeLabel(status: HttpStatusCode): String {
        return if (status is HttpStatus) {
            status.name
        } else {
            "HTTP_${status.value()}"
        }
    }
}
