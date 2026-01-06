package rubit.coresecurityoauth2.dto

data class OAuth2TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
