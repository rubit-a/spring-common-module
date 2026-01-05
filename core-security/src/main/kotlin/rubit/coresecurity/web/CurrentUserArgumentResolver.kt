package rubit.coresecurity.web

import org.springframework.core.MethodParameter
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.security.Principal

class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val annotation = parameter.getParameterAnnotation(CurrentUser::class.java)
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            if (annotation?.required == true) {
                throw IllegalStateException("No authenticated user")
            }
            return null
        }

        val parameterType = parameter.parameterType
        return when {
            Authentication::class.java.isAssignableFrom(parameterType) -> authentication
            Principal::class.java.isAssignableFrom(parameterType) -> authentication
            String::class.java.isAssignableFrom(parameterType) -> authentication.name
            UserDetails::class.java.isAssignableFrom(parameterType) -> authentication.principal as? UserDetails
            else -> authentication.principal
        }
    }
}
