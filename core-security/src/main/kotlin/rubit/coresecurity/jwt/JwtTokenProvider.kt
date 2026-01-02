package rubit.coresecurity.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import rubit.coresecurity.config.JwtProperties
import java.util.*
import javax.crypto.SecretKey

class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())

    fun generateAccessToken(authentication: Authentication): String {
        return generateToken(
            subject = authentication.name,
            claims = mapOf("authorities" to authentication.authorities.map { it.authority }),
            expiration = jwtProperties.accessTokenExpiration
        )
    }

    fun generateAccessToken(username: String, authorities: Collection<String> = emptyList()): String {
        return generateToken(
            subject = username,
            claims = mapOf("authorities" to authorities),
            expiration = jwtProperties.accessTokenExpiration
        )
    }

    fun generateRefreshToken(username: String): String {
        return generateToken(
            subject = username,
            claims = emptyMap(),
            expiration = jwtProperties.refreshTokenExpiration
        )
    }

    private fun generateToken(
        subject: String,
        claims: Map<String, Any>,
        expiration: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(subject)
            .claims(claims)
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtProperties.issuer)
            .signWith(secretKey)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        return getClaims(token).subject
    }

    fun getAuthoritiesFromToken(token: String): List<String> {
        val claims = getClaims(token)
        @Suppress("UNCHECKED_CAST")
        return claims["authorities"] as? List<String> ?: emptyList()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
