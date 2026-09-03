package ru.wolfram.feed.controller

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.JsonWriter
import ru.wolfram.feed.dto.FeedItemResponse
import ru.wolfram.feed.service.FeedService

@Component
@HttpController
class FeedController(
    private val feedService: FeedService,
    private val jsonWriter: JsonWriter<List<FeedItemResponse>>
) {
    private val logger = LoggerFactory.getLogger(FeedController::class.java)

    @HttpRoute(method = "GET", path = "/v1/feed")
    suspend fun getUserFeed(request: HttpServerRequest): HttpServerResponse {
        val userId = request.queryParams()["user_id"]?.firstOrNull()?.toLongOrNull()
            ?: return errorResponse(400, "user_id is required and must be a number")

        val page = request.queryParams()["page"]?.firstOrNull()?.toIntOrNull() ?: 0
        val size = request.queryParams()["size"]?.firstOrNull()?.toIntOrNull() ?: 20

        logger.info("GET /v1/feed user_id={} page={} size={}", userId, page, size)

        val feed = feedService.getUserFeed(userId, page, size)

        return HttpServerResponse.of(
            200,
            HttpHeaders.of("Content-Type", "application/json"),
            HttpBody.of(jsonWriter.toByteArray(feed))
        )
    }

    private fun errorResponse(status: Int, message: String): HttpServerResponse {
        val errorJson = """{"error":"$message"}"""
        return HttpServerResponse.of(
            status,
            HttpHeaders.of("Content-Type", "application/json"),
            HttpBody.of(errorJson.toByteArray())
        )
    }
}