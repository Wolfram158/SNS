package ru.wolfram.feed.kafka

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener
import ru.wolfram.feed.dto.PostCreatedMessage
import ru.wolfram.feed.service.FeedService

@Component
class PostCreatedListener(
    private val feedService: FeedService
) {
    private val logger = LoggerFactory.getLogger(PostCreatedListener::class.java)

    @KafkaListener("kafka.consumer.feed-listener")
    suspend fun process(@Json event: PostCreatedMessage?, exception: Exception?) {
        if (exception != null) {
            logger.error("Failed to deserialize PostCreated message", exception)
            return
        }

        if (event == null) {
            logger.warn("Received null PostCreated message")
            return
        }

        logger.info(
            "Received PostCreated: postId={}, authorId={}",
            event.postId, event.authorId
        )

        try {
            feedService.handlePostCreated(event)
        } catch (e: Exception) {
            logger.error(
                "Failed to process PostCreated: postId={}",
                event.postId, e
            )
        }
    }
}