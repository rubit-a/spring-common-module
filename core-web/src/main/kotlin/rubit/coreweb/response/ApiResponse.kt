package rubit.coreweb.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
) {
    companion object {
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun error(error: ApiError): ApiResponse<Nothing> {
            return ApiResponse(success = false, error = error)
        }
    }
}
