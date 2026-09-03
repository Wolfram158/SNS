package ru.wolfram.gatewayspring.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtValidator(
    @param:Value("\${jwt.secret}") private val secret: String
) {
    private val key: SecretKey by lazy {
        val decodedKey = Base64.getDecoder().decode(secret)
        Keys.hmacShaKeyFor(decodedKey)
    }

    fun validate(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: Exception) {
            null
        }
    }

    fun extractUserId(token: String): Long? {
        val claims = validate(token) ?: return null
        return try {
            claims["userId"]?.toString()?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }
}