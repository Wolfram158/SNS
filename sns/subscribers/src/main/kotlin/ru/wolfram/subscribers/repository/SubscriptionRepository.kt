package ru.wolfram.subscribers.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.subscribers.dao.SubscriptionDAO

@Repository
interface SubscriptionRepository : R2dbcRepository {
    @Query(
        """
        INSERT INTO subscriptions (follower_id, following_id)
        VALUES (:sub.followerId, :sub.followingId)
        ON CONFLICT DO NOTHING
    """
    )
    suspend fun save(sub: SubscriptionDAO): UpdateCount

    @Query(
        """
        DELETE FROM subscriptions
        WHERE follower_id = :followerId
          AND following_id = :followingId
    """
    )
    suspend fun delete(
        followerId: Long,
        followingId: Long
    ): UpdateCount

    @Query(
        """
        SELECT following_id FROM subscriptions
        WHERE follower_id = :followerId
        """
    )
    suspend fun findFollowingByFollower(followerId: Long): List<Long>

    @Query(
        """
        SELECT follower_id FROM subscriptions
        WHERE following_id = :followingId
        """
    )
    suspend fun findFollowersByFollowing(followingId: Long): List<Long>
}