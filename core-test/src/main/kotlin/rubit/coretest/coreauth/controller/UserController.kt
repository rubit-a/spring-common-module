package rubit.coretest.coreauth.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/me")
    fun getCurrentUser(): Map<String, Any> {
        val authentication = SecurityContextHolder.getContext().authentication

        return mapOf(
            "username" to (authentication?.name ?: "anonymous"),
            "authorities" to (authentication?.authorities?.map { it.authority } ?: emptyList<String>()),
            "authenticated" to (authentication?.isAuthenticated ?: false)
        )
    }

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): Map<String, Any> {
        return mapOf(
            "username" to authentication.name,
            "authorities" to authentication.authorities.map { it.authority },
            "message" to "This is a protected endpoint. You are authenticated!"
        )
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun adminOnly(authentication: Authentication): Map<String, String> {
        return mapOf(
            "message" to "Welcome admin: ${authentication.name}",
            "level" to "ADMIN_ONLY"
        )
    }
}
