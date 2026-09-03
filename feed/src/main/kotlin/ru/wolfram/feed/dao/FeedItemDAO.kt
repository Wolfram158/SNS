package ru.wolfram.feed.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("feed_items")
data class FeedItemDAO(
    @param:Id @param:Column("id") val id: Long,
    @param:Column("post_id") val postId: UUID,
    @param:Column("author_id") val authorId: Long,
    @param:Column("text") val text: String,
    @param:Column("image_keys") val imageKeys: String,
    @param:Column("created_at") val createdAt: OffsetDateTime
)