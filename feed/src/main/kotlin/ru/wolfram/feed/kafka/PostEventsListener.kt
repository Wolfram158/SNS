package ru.wolfram.feed.kafka

import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.kafka.common.annotation.KafkaListener
import ru.wolfram.common.PostEvent
import ru.wolfram.feed.service.FeedService

@Component
class PostEventsListener(
    private val feedService: FeedService
) {
    private val logger = LoggerFactory.getLogger(PostEventsListener::class.java)

    @KafkaListener("kafka.consumer.feed-listener")
    suspend fun process(@Json event: PostEvent?, exception: Exception?) {
        if (exception != null) {
            logger.error("Failed to deserialize PostCreated message", exception)
            return
        }

        if (event == null) {
            logger.warn("Received null PostCreated message")
            return
        }

        try {
            when (event) {
                is PostEvent.PostCreatedEvent -> feedService.handlePostCreated(event)
                is PostEvent.PostDeletedEvent -> feedService.handlePostDeleted(event)
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to process post event",
                e
            )
        }
    }
}