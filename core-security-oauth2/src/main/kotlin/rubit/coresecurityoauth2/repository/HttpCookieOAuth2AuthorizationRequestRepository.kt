package rubit.coresecurityoauth2.repository

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import rubit.coresecurityoauth2.config.OAuth2Properties
import rubit.coresecurityoauth2.util.CookieUtils

class HttpCookieOAuth2AuthorizationRequestRepository(
    private val properties: OAuth2Properties
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        private const val AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        private const val REDIRECT_URI_COOKIE_NAME = "redirect_uri"
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = CookieUtils.getCookie(request, AUTH_REQUEST_COOKIE_NAME) ?: return null
        return CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest::class.java)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response)
            return
        }

        val serialized = CookieUtils.serialize(authorizationRequest)
        CookieUtils.addCookie(response, AUTH_REQUEST_COOKIE_NAME, serialized, properties.cookieExpireSeconds)

        val redirectUri = request.getParameter(properties.redirectParamName)
        if (!redirectUri.isNullOrBlank()) {
            CookieUtils.addCookie(response, REDIRECT_URI_COOKIE_NAME, redirectUri, properties.cookieExpireSeconds)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        val authorizationRequest = loadAuthorizationRequest(request)
        removeAuthorizationRequestCookies(request, response)
        return authorizationRequest
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        CookieUtils.deleteCookie(request, response, AUTH_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_COOKIE_NAME)
    }

    fun getRedirectUriFromCookies(request: HttpServletRequest): String? {
        return CookieUtils.getCookie(request, REDIRECT_URI_COOKIE_NAME)?.value
    }
}
