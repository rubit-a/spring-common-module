package rubit.testweb.commonauth.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*
import rubit.coresecurity.jwt.JwtTokenProvider
import rubit.testweb.commonauth.dto.LoginRequest
import rubit.testweb.commonauth.dto.TokenResponse
import rubit.testweb.commonauth.dto.UserInfoResponse

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "auth", name = ["mode"], havingValue = "jwt", matchIfMissing = true)
class JwtAuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val authenticationManager: AuthenticationManager
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        val accessToken = jwtTokenProvider.generateAccessToken(authentication)

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            username = authentication.name
        )

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @GetMapping("/validate")
    fun validateToken(@RequestParam token: String): Map<String, Boolean> {
        return mapOf("valid" to jwtTokenProvider.validateToken(token))
    }

    @GetMapping("/user-info")
    fun getUserInfo(@RequestParam token: String): UserInfoResponse {
        val username = jwtTokenProvider.getUsernameFromToken(token)
        val authorities = jwtTokenProvider.getAuthoritiesFromToken(token)

        return UserInfoResponse(
            username = username,
            authorities = authorities
        )
    }
}
