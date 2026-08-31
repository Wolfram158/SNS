package ru.wolfram.gatewayspring.filters

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.wolfram.gatewayspring.constants.Constants
import ru.wolfram.gatewayspring.jwt.JwtValidator

@Component
class JwtGatewayFilterFactory(
    private val jwtValidator: JwtValidator
) : AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config>(Config::class.java) {
    class Config

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader == null || !authHeader.startsWith(Constants.BEARER_PREFIX)) {
                return@GatewayFilter onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED)
            }

            val token = authHeader.substring(Constants.BEARER_PREFIX.length)
            jwtValidator.validate(token) ?: return@GatewayFilter onError(
                exchange,
                "Invalid or expired token",
                HttpStatus.UNAUTHORIZED
            )

            chain.filter(exchange)
        }
    }

    private fun onError(exchange: ServerWebExchange, message: String, status: HttpStatus): Mono<Void> {
        exchange.response.statusCode = status
        exchange.response.headers.add("Content-Type", "application/json")
        val jsonBody = "{\"error\": \"$message\"}"
        val buffer = exchange.response.bufferFactory().wrap(jsonBody.toByteArray())
        return exchange.response.writeWith(Mono.just(buffer))
    }
}