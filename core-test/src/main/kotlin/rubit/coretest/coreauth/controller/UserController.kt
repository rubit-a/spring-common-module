package rubit.coretest.coreauth.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rubit.coresecurity.util.SecurityContextUtils
import rubit.coresecurity.web.CurrentUser

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

    @GetMapping("/current")
    fun currentUser(@CurrentUser username: String?): Map<String, String?> {
        return mapOf("username" to username)
    }

    @GetMapping("/current-auth")
    fun currentUserAuthentication(@CurrentUser authentication: Authentication?): Map<String, Any?> {
        return mapOf(
            "username" to authentication?.name,
            "authorities" to authentication?.authorities?.map { it.authority }
        )
    }

    @GetMapping("/current-details")
    fun currentUserDetails(@CurrentUser userDetails: UserDetails?): Map<String, Any?> {
        return mapOf(
            "username" to userDetails?.username,
            "authorities" to userDetails?.authorities?.map { it.authority }
        )
    }

    @GetMapping("/context")
    fun currentUserFromContext(): Map<String, Any?> {
        return mapOf(
            "username" to SecurityContextUtils.getUsername(),
            "authorities" to SecurityContextUtils.getAuthorities(),
            "authenticated" to SecurityContextUtils.isAuthenticated()
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
