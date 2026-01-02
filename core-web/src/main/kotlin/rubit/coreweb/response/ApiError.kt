package rubit.coreweb.response

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null
)
