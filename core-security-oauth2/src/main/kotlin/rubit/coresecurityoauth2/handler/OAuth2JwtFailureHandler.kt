package rubit.coresecurityoauth2.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import rubit.coresecurityoauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository
import java.nio.charset.StandardCharsets

class OAuth2JwtFailureHandler(
    private val authorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,
    private val objectMapper: ObjectMapper
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = StandardCharsets.UTF_8.name()
        val body = mapOf(
            "error" to "oauth2_auth_failed",
            "message" to (exception.message ?: "OAuth2 authentication failed")
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
