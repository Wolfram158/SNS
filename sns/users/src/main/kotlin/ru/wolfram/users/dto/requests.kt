package ru.wolfram.users.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class RegisterRequest(
    val nickname: String,
    val password: String
)

@Json
data class LoginRequest(
    val nickname: String,
    val password: String
)

@Json
data class RefreshRequest(
    val refreshToken: String
)