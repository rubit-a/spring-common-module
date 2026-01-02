package rubit.coreweb.response

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val defaultMessage: String
) {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request body"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "Missing request parameter"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", "Request parameter type mismatch"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Unsupported media type"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error");

    companion object {
        fun fromStatus(status: HttpStatusCode): ErrorCode? {
            val httpStatus = status as? HttpStatus ?: return null
            return when (httpStatus) {
                HttpStatus.BAD_REQUEST -> INVALID_REQUEST
                HttpStatus.UNAUTHORIZED -> UNAUTHORIZED
                HttpStatus.FORBIDDEN -> FORBIDDEN
                HttpStatus.NOT_FOUND -> NOT_FOUND
                HttpStatus.METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED
                HttpStatus.UNSUPPORTED_MEDIA_TYPE -> UNSUPPORTED_MEDIA_TYPE
                HttpStatus.INTERNAL_SERVER_ERROR -> INTERNAL_ERROR
                else -> null
            }
        }
    }
}
