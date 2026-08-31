package ru.wolfram.users.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import ru.tinkoff.kora.common.Component
import ru.wolfram.users.config.JwtConfig
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
    private val config: JwtConfig
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(config.secret()))
    }

    private val secureRandom = SecureRandom()

    fun generateAccessToken(userId: Long, nickname: String): String {
        val now = Instant.now()
        return Jwts.builder()
            .claim("userId", userId)
            .claim("nickname", nickname)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(config.accessTokenTtlSeconds())))
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(): String {
        val bytes = ByteArray(48)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}