package ru.wolfram.users.config

import ru.tinkoff.kora.config.common.annotation.ConfigSource

@ConfigSource("jwt")
interface JwtConfig {
    fun secret(): String
    fun accessTokenTtlSeconds(): Long
    fun refreshTokenTtlSeconds(): Long
}