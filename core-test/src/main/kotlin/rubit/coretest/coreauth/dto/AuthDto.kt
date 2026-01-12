package rubit.coretest.coreauth.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class UserInfoResponse(
    val username: String,
    val authorities: List<String>
)

data class JwtConfigResponse(
    val issuer: String,
    val audience: String?,
    val clockSkewSeconds: Long,
    val secretKeyFormat: String
)
