package ru.wolfram.subscribers.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Table

@Table("subscriptions")
data class SubscriptionDAO(
    @param:Column("follower_id") val followerId: Long,
    @param:Column("following_id") val followingId: Long
)