package ru.wolfram.subscribers.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.wolfram.subscribers.dao.SubscriptionDAO
import ru.wolfram.subscribers.dto.SubscribeRequest
import ru.wolfram.subscribers.dto.SubscribeResponse
import ru.wolfram.subscribers.repository.SubscriptionRepository

@Component
class SubscriptionService(
    private val repository: SubscriptionRepository
) {
    suspend fun subscribe(request: SubscribeRequest): SubscribeResponse {
        if (request.followerId == request.followingId) {
            throw HttpServerResponseException.of(400, "Cannot subscribe to yourself")
        }

        val dao = SubscriptionDAO(
            followerId = request.followerId,
            followingId = request.followingId
        )

        val updateCount = repository.save(dao)
        if (updateCount.value() == 0L) {
            throw HttpServerResponseException.of(409, "Subscription already exists")
        }

        return SubscribeResponse("OK")
    }

    suspend fun unsubscribe(followerId: Long, followingId: Long) {
        val updateCount = repository.delete(followerId, followingId)
        if (updateCount.value() == 0L) {
            throw HttpServerResponseException.of(404, "Subscription not found")
        }
    }

    suspend fun getFollowing(userId: Long): List<Long> {
        return repository.findFollowingByFollower(userId)
    }

    suspend fun getFollowers(authorId: Long): List<Long> {
        return repository.findFollowersByFollowing(authorId)
    }
}