package rubit.coresecurity.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import rubit.coresecurity.config.JwtKeyFormat
import rubit.coresecurity.config.JwtProperties
import java.util.*
import java.util.regex.Pattern
import javax.crypto.SecretKey

class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey
    private val jwtParser: JwtParser

    init {
        val secretKeyBytes = decodeSecretKeyBytes(jwtProperties)
        secretKey = Keys.hmacShaKeyFor(secretKeyBytes)
        jwtParser = buildParser(secretKey, jwtProperties)
    }

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

        val builder = Jwts.builder()
            .subject(subject)
            .claims(claims)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)

        if (jwtProperties.issuer.isNotBlank()) {
            builder.issuer(jwtProperties.issuer)
        }

        jwtProperties.audience
            ?.takeIf { it.isNotBlank() }
            ?.let { audience -> builder.audience().add(audience).and() }

        return builder.compact()
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
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return jwtParser
            .parseSignedClaims(token)
            .payload
    }

    private fun buildParser(secretKey: SecretKey, properties: JwtProperties): JwtParser {
        require(properties.clockSkewSeconds >= 0) {
            "jwt.clock-skew-seconds must be 0 or greater"
        }

        val builder = Jwts.parser()
            .verifyWith(secretKey)
            .setAllowedClockSkewSeconds(properties.clockSkewSeconds)

        if (properties.issuer.isNotBlank()) {
            builder.requireIssuer(properties.issuer)
        }

        properties.audience
            ?.takeIf { it.isNotBlank() }
            ?.let { audience -> builder.requireAudience(audience) }

        return builder.build()
    }

    private fun decodeSecretKeyBytes(properties: JwtProperties): ByteArray {
        val raw = properties.secretKey
        require(raw.isNotBlank()) { "jwt.secret-key must not be blank" }

        val bytes = when (properties.secretKeyFormat) {
            JwtKeyFormat.RAW -> raw.toByteArray(Charsets.UTF_8)
            JwtKeyFormat.BASE64 -> decodeBase64(raw)
            JwtKeyFormat.HEX -> decodeHex(raw)
        }

        require(bytes.size >= MIN_HMAC_KEY_BYTES) {
            "jwt.secret-key must be at least $MIN_HMAC_KEY_BYTES bytes for HS256"
        }

        return bytes
    }

    private fun decodeBase64(value: String): ByteArray {
        return try {
            Decoders.BASE64.decode(value.trim())
        } catch (ex: Exception) {
            throw IllegalArgumentException("jwt.secret-key must be valid Base64 when using BASE64 format", ex)
        }
    }

    private fun decodeHex(value: String): ByteArray {
        val sanitized = value.trim()
        require(sanitized.length % 2 == 0) {
            "jwt.secret-key must have an even number of hex characters when using HEX format"
        }
        require(HEX_PATTERN.matcher(sanitized).matches()) {
            "jwt.secret-key must be valid hex when using HEX format"
        }

        val result = ByteArray(sanitized.length / 2)
        for (index in result.indices) {
            val start = index * 2
            result[index] = sanitized.substring(start, start + 2).toInt(16).toByte()
        }
        return result
    }

    companion object {
        private const val MIN_HMAC_KEY_BYTES = 32
        private val HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$")
    }
}
