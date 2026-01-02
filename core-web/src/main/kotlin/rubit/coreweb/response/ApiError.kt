package rubit.coreweb.response

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null
) {
    companion object {
        fun from(
            errorCode: ErrorCode,
            message: String? = null,
            details: Map<String, Any?>? = null
        ): ApiError {
            return ApiError(
                code = errorCode.code,
                message = message ?: errorCode.defaultMessage,
                details = details
            )
        }
    }
}
