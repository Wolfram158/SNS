package ru.wolfram.feed.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonField
import java.time.OffsetDateTime
import java.util.UUID

@Json
data class FeedItemResponse(
    @param:JsonField("post_id") val postId: UUID,
    @param:JsonField("author_id") val authorId: Long,
    val text: String,
    @param:JsonField("image_urls") val imageUrls: List<String>,
    @param:JsonField("created_at") val createdAt: OffsetDateTime
)