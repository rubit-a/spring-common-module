package rubit.coreweb.response

import org.springframework.core.MethodParameter
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import rubit.coreweb.config.CoreWebProperties

@ControllerAdvice
class ApiResponseBodyAdvice(
    private val properties: CoreWebProperties
) : ResponseBodyAdvice<Any> {

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: org.springframework.http.MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        if (body is ApiResponse<*>) {
            return body
        }

        if (body is ProblemDetail) {
            return body
        }

        if (returnType.parameterType == ResponseEntity::class.java ||
            returnType.parameterType == HttpEntity::class.java
        ) {
            return body
        }

        if (body == null) {
            return if (properties.response.wrapNull) ApiResponse.success(null) else null
        }

        if (body is String || body is HttpStatusCode) {
            return body
        }

        return ApiResponse.success(body)
    }
}
