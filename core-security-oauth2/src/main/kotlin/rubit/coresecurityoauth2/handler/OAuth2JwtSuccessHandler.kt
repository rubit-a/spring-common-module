package rubit.coresecurityoauth2.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import rubit.coresecurity.config.JwtProperties
import rubit.coresecurity.jwt.JwtTokenProvider
import rubit.coresecurityoauth2.config.OAuth2Properties
import rubit.coresecurityoauth2.config.ResponseMode
import rubit.coresecurityoauth2.dto.OAuth2TokenResponse
import rubit.coresecurityoauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository
import java.net.URI
import java.nio.charset.StandardCharsets

class OAuth2JwtSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val oauth2Properties: OAuth2Properties,
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val objectMapper: ObjectMapper
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (response.isCommitted) {
            authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
            return
        }

        val username = resolveUsername(authentication)
        val authorities = resolveAuthorities(authentication)

        val accessToken = jwtTokenProvider.generateAccessToken(username, authorities)
        val refreshToken = jwtTokenProvider.generateRefreshToken(username)

        val tokenResponse = OAuth2TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000
        )

        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)

        if (oauth2Properties.responseMode == ResponseMode.REDIRECT) {
            val targetUrl = buildRedirectUrl(request, tokenResponse)
            if (targetUrl != null) {
                response.sendRedirect(targetUrl)
                return
            }
        }

        response.status = HttpServletResponse.SC_OK
        response.contentType = "application/json"
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.write(objectMapper.writeValueAsString(tokenResponse))
    }

    private fun buildRedirectUrl(
        request: HttpServletRequest,
        tokenResponse: OAuth2TokenResponse
    ): String? {
        val redirectUri = authorizationRequestRepository.getRedirectUriFromCookies(request)
        val resolvedRedirectUri = when {
            !redirectUri.isNullOrBlank() && isAuthorizedRedirectUri(redirectUri) -> redirectUri
            else -> oauth2Properties.successRedirectUri
        }

        if (resolvedRedirectUri.isNullOrBlank()) {
            return null
        }

        return UriComponentsBuilder.fromUriString(resolvedRedirectUri)
            .queryParam("access_token", tokenResponse.accessToken)
            .queryParam("refresh_token", tokenResponse.refreshToken)
            .queryParam("token_type", tokenResponse.tokenType)
            .queryParam("expires_in", tokenResponse.expiresIn)
            .build()
            .toUriString()
    }

    private fun resolveUsername(authentication: Authentication): String {
        val principalAttribute = oauth2Properties.principalAttribute
        val principal = authentication.principal
        if (principal is OAuth2User && !principalAttribute.isNullOrBlank()) {
            val attribute = principal.getAttribute<String>(principalAttribute)
            if (!attribute.isNullOrBlank()) {
                return attribute
            }
        }
        return authentication.name
    }

    private fun resolveAuthorities(authentication: Authentication): List<String> {
        val authorities = authentication.authorities.mapNotNull { it.authority }
        return if (authorities.isEmpty()) {
            oauth2Properties.defaultAuthorities
        } else {
            authorities
        }
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        if (oauth2Properties.authorizedRedirectUris.isEmpty()) {
            return true
        }

        val clientUri = URI.create(uri)
        return oauth2Properties.authorizedRedirectUris.any { authorizedUri ->
            val allowed = URI.create(authorizedUri)
            allowed.scheme.equals(clientUri.scheme, ignoreCase = true) &&
                allowed.host.equals(clientUri.host, ignoreCase = true) &&
                allowed.port == clientUri.port
        }
    }
}
