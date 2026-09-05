package ru.wolfram.feed.service

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.client.common.HttpClientException
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.JsonWriter
import ru.wolfram.common.PostEvent
import ru.wolfram.feed.client.SubscribersClient
import ru.wolfram.feed.dao.FeedItemDAO
import ru.wolfram.feed.dto.FeedItemResponse
import ru.wolfram.feed.repository.FeedRepository
import ru.wolfram.feed.s3.S3Service

@Component
class FeedService(
    private val feedRepository: FeedRepository,
    private val subscribersClient: SubscribersClient,
    private val s3Service: S3Service,
    private val imagesKeysReader: JsonReader<List<String>>,
    private val imageKeysWriter: JsonWriter<List<String>>
) {
    private val logger = LoggerFactory.getLogger(FeedService::class.java)

    suspend fun handlePostCreated(message: PostEvent.PostCreatedEvent) {
        validatePostCreated(message)

        if (feedRepository.exists(message.postId) > 0) {
            logger.info("Post {} already exists, skipping", message.postId)
            return
        }

        val imageKeysJson = if (message.imageKeys.isDefined && !message.imageKeys.isNull) {
            imageKeysWriter.toString(message.imageKeys.value())
        } else {
            imageKeysWriter.toString(emptyList())
        }

        feedRepository.save(
            postId = message.postId,
            authorId = message.authorId,
            text = message.text,
            imageKeys = imageKeysJson,
            createdAt = message.createdAt
        )

        logger.info(
            "Post saved: postId={}, authorId={}",
            message.postId, message.authorId
        )
    }

    suspend fun handlePostDeleted(message: PostEvent.PostDeletedEvent) {
        feedRepository.deleteByPostId(message.postId)

        logger.info(
            "Post deleted: postId={}, authorId={}",
            message.postId, message.authorId
        )
    }

    private fun validatePostCreated(message: PostEvent.PostCreatedEvent) {
        require(message.text.isNotBlank()) {
            "Post text cannot be blank (postId=${message.postId})"
        }
        require(message.authorId > 0) {
            "Invalid authorId=${message.authorId}"
        }
    }

    suspend fun getUserFeed(userId: Long, page: Int, size: Int): List<FeedItemResponse> {
        require(userId > 0) { "Invalid userId=$userId" }
        require(page >= 0) { "Page must be non-negative" }
        require(size in 1..MAX_PAGE_SIZE) { "Size must be 1..$MAX_PAGE_SIZE" }

        val following = try {
            subscribersClient.getFollowing(userId)
        } catch (e: HttpClientException) {
            logger.error(
                "Failed to fetch following for userId={}",
                userId, e
            )
            emptyList()
        }

        val authorIds = following.distinct()

        if (authorIds.isEmpty()) {
            return emptyList()
        }

        val offset = page * size
        val daoList = feedRepository.findByAuthorIds(authorIds, offset, size)

        logger.debug(
            "Fetched feed: userId={}, following={}, page={}, size={}, result={}",
            userId, following.size, page, size, daoList.size
        )

        return daoList.map { it.toResponse() }
    }

    private fun FeedItemDAO.toResponse(): FeedItemResponse {
        val keys = imagesKeysReader.read(this.imageKeys) ?: listOf()
        val urls = s3Service.generatePresignedGetUrls(keys)

        return FeedItemResponse(
            postId = this.postId,
            authorId = this.authorId,
            text = this.text,
            imageUrls = urls,
            createdAt = this.createdAt
        )
    }

    companion object {
        private const val MAX_PAGE_SIZE = 50
    }
}