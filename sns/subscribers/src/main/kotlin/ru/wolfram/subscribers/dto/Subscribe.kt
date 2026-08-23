package ru.wolfram.subscribers.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonField

@Json
data class SubscribeRequest(
    @param:JsonField("follower_id") val followerId: Long,
    @param:JsonField("following_id") val followingId: Long
)

@Json
data class UnsubscribeRequest(
    @param:JsonField("follower_id") val followerId: Long,
    @param:JsonField("following_id") val followingId: Long
)

@Json
data class SubscribeResponse(
    @param:JsonField("msg") val msg: String
)