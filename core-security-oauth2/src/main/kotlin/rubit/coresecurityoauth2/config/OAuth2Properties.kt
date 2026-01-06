package rubit.coresecurityoauth2.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "auth.oauth2")
@Validated
data class OAuth2Properties(
    val enabled: Boolean = false,
    val responseMode: ResponseMode = ResponseMode.JSON,
    val successRedirectUri: String? = null,
    val authorizedRedirectUris: List<String> = emptyList(),
    val redirectParamName: String = "redirect_uri",
    val principalAttribute: String? = null,
    val defaultAuthorities: List<String> = listOf("ROLE_USER"),
    val cookieExpireSeconds: Int = 180
)

enum class ResponseMode {
    JSON,
    REDIRECT
}
