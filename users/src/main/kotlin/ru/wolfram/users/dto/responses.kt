package ru.wolfram.users.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.OffsetDateTime

@Json
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserResponse
)

@Json
data class UserResponse(
    val id: Long,
    val nickname: String,
    val createdAt: OffsetDateTime
)

@Json
data class ErrorResponse(
    val error: String
)