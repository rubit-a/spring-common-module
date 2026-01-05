package rubit.coresecurity.util

import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

object SecurityContextUtils {

    fun getAuthentication(): Authentication? = SecurityContextHolder.getContext().authentication

    fun isAuthenticated(): Boolean {
        val authentication = getAuthentication() ?: return false
        if (authentication is AnonymousAuthenticationToken) {
            return false
        }
        return authentication.isAuthenticated
    }

    fun getUsername(): String? {
        return if (isAuthenticated()) getAuthentication()?.name else null
    }

    fun getAuthorities(): List<String> {
        val authentication = getAuthentication() ?: return emptyList()
        if (authentication is AnonymousAuthenticationToken) {
            return emptyList()
        }
        return authentication.authorities.mapNotNull { it.authority }
    }

    fun <T> getPrincipal(expectedType: Class<T>): T? {
        val authentication = getAuthentication() ?: return null
        val principal = authentication.principal ?: return null
        return if (expectedType.isInstance(principal)) expectedType.cast(principal) else null
    }
}
