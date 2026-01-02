package rubit.testweb.coreauth.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rubit.testweb.coreauth.dto.LoginRequest
import rubit.testweb.coreauth.dto.UserInfoResponse

@RestController
@RequestMapping("/api/session")
@ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "session")
class SessionAuthController(
    private val authenticationManager: AuthenticationManager
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): UserInfoResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        httpRequest.session

        return UserInfoResponse(
            username = authentication.name,
            authorities = authentication.authorities.mapNotNull { it.authority }
        )
    }

    @PostMapping("/logout")
    fun logout(httpRequest: HttpServletRequest): Map<String, String> {
        httpRequest.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
        return mapOf("message" to "logged out")
    }
}
