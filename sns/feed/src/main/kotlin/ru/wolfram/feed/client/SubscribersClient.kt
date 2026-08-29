package ru.wolfram.feed.client

import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.json.common.annotation.Json

@HttpClient(configPath = "httpClient.subscribersClient")
interface SubscribersClient {
    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/v1/following")
    suspend fun getFollowing(@Query("user_id") userId: Long): List<Long>

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/v1/followers")
    suspend fun getFollowers(@Query("author_id") authorId: Long): List<Long>
}