package ru.wolfram.users.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import ru.wolfram.users.dto.AuthResponse
import ru.wolfram.users.dto.LoginRequest
import ru.wolfram.users.dto.RefreshRequest
import ru.wolfram.users.dto.RegisterRequest
import ru.wolfram.users.service.AuthService

@Component
@HttpController("/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @HttpRoute(method = HttpMethod.POST, path = "/register")
    @Json
    suspend fun register(@Json request: RegisterRequest): AuthResponse {
        return authService.register(request)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/login")
    @Json
    suspend fun login(@Json request: LoginRequest): AuthResponse {
        return authService.login(request)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/refresh")
    @Json
    suspend fun refresh(@Json request: RefreshRequest): AuthResponse {
        return authService.refresh(request)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/logout")
    suspend fun logout(@Json request: RefreshRequest) {
        authService.logout(request)
    }
}