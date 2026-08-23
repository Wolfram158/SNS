package ru.wolfram.subscribers.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json
import ru.wolfram.subscribers.dto.SubscribeRequest
import ru.wolfram.subscribers.dto.SubscribeResponse
import ru.wolfram.subscribers.service.SubscriptionService

@Component
@HttpController
class SubscriptionController(
    private val service: SubscriptionService
) {
    @HttpRoute(method = HttpMethod.POST, path = "/v1/subscribe")
    @Json
    suspend fun subscribe(@Json request: SubscribeRequest): SubscribeResponse {
        return service.subscribe(request)
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/v1/unsubscribe")
    suspend fun unsubscribe(
        @Query("follower_id") followerId: Long,
        @Query("following_id") followingId: Long
    ) {
        service.unsubscribe(followerId, followingId)
    }
}