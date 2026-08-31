package ru.wolfram.gatewayspring.filters

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import ru.wolfram.gatewayspring.constants.Constants
import ru.wolfram.gatewayspring.jwt.JwtValidator

@Component
class AddUserIdGatewayFilterFactory(
    private val jwtValidator: JwtValidator
) : AbstractGatewayFilterFactory<AddUserIdGatewayFilterFactory.Config>(Config::class.java) {
    class Config

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
                val token = authHeader.substring(Constants.BEARER_PREFIX.length)
                val userId = jwtValidator.extractUserId(token)

                if (userId != null) {
                    val mutatedRequest = exchange.request.mutate()
                        .header(Constants.USER_ID_HEADER, userId.toString())
                        .build()

                    val mutatedExchange = exchange.mutate().request(mutatedRequest).build()
                    return@GatewayFilter chain.filter(mutatedExchange)
                }
            }

            chain.filter(exchange)
        }
    }
}