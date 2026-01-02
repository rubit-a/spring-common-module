package rubit.testweb.commonauth.controller

import org.springframework.web.bind.annotation.*
import rubit.commonauth.jwt.JwtTokenProvider
import rubit.testweb.commonauth.dto.LoginRequest
import rubit.testweb.commonauth.dto.TokenResponse
import rubit.testweb.commonauth.dto.UserInfoResponse

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        // In a real application, you would verify the credentials against a database
        // For this demo, we'll accept any credentials with username length > 3
        if (request.username.length < 3) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val authorities = if (request.username == "admin") {
            listOf("ROLE_ADMIN", "ROLE_USER")
        } else {
            listOf("ROLE_USER")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            username = request.username,
            authorities = authorities
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            username = request.username
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
