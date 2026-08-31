package ru.wolfram.users.service

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.wolfram.users.config.JwtConfig
import ru.wolfram.users.dao.UserDAO
import ru.wolfram.users.dto.AuthResponse
import ru.wolfram.users.dto.LoginRequest
import ru.wolfram.users.dto.RefreshRequest
import ru.wolfram.users.dto.RegisterRequest
import ru.wolfram.users.dto.UserResponse
import ru.wolfram.users.repository.RefreshTokenRepository
import ru.wolfram.users.repository.UserRepository
import java.time.OffsetDateTime

@Component
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordService: PasswordService,
    private val jwtService: JwtService,
    private val jwtConfig: JwtConfig
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    companion object {
        private val NICKNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,32}$")
        private const val MIN_PASSWORD_LENGTH = 8
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        if (!NICKNAME_REGEX.matches(request.nickname)) {
            throw HttpServerResponseException.of(
                400,
                "Nickname must be 3-32 characters (letters, digits, underscore)"
            )
        }

        if (request.password.length < MIN_PASSWORD_LENGTH) {
            throw HttpServerResponseException.of(
                400,
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            )
        }

        val existing = userRepository.findByNickname(request.nickname)
        if (existing != null) {
            throw HttpServerResponseException.of(409, "Nickname already taken")
        }

        val passwordHash = passwordService.hash(request.password)

        val now = OffsetDateTime.now()
        val user = userRepository.insert(
            nickname = request.nickname,
            passwordHash = passwordHash,
            createdAt = now,
            updatedAt = now
        )

        logger.info("User registered: id={}, nickname={}", user.id, user.nickname)

        return generateTokens(user)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByNickname(request.nickname)
        if (user == null || !passwordService.verify(request.password, user.passwordHash)) {
            throw HttpServerResponseException.of(401, "Invalid credentials")
        }

        logger.info("User logged in: id={}, nickname={}", user.id, user.nickname)

        return generateTokens(user)
    }

    suspend fun refresh(request: RefreshRequest): AuthResponse {
        val token = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw HttpServerResponseException.of(401, "Invalid refresh token")

        if (token.expiresAt.isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.deleteByToken(token.token)
            throw HttpServerResponseException.of(401, "Refresh token expired")
        }

        val user = userRepository.findById(token.userId)
            ?: throw HttpServerResponseException.of(401, "User not found")

        refreshTokenRepository.deleteByToken(token.token)

        logger.info("Tokens refreshed for user: id={}", user.id)

        return generateTokens(user)
    }

    suspend fun logout(request: RefreshRequest) {
        refreshTokenRepository.deleteByToken(request.refreshToken)
        logger.info("Refresh token revoked")
    }

    private suspend fun generateTokens(user: UserDAO): AuthResponse {
        val accessToken = jwtService.generateAccessToken(user.id, user.nickname)
        val refreshToken = jwtService.generateRefreshToken()

        val now = OffsetDateTime.now()
        refreshTokenRepository.insert(
            token = refreshToken,
            userId = user.id,
            expiresAt = now.plusSeconds(jwtConfig.refreshTokenTtlSeconds()),
            createdAt = now
        )

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = jwtConfig.accessTokenTtlSeconds(),
            user = UserResponse(
                id = user.id,
                nickname = user.nickname,
                createdAt = user.createdAt
            )
        )
    }
}