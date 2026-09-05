package ru.wolfram.common

import ru.tinkoff.kora.json.common.JsonNullable
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorField
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorValue
import ru.tinkoff.kora.json.common.annotation.JsonField
import java.time.OffsetDateTime
import java.util.*

@Json
@JsonDiscriminatorField("event_type")
sealed interface PostEvent {
    @JsonDiscriminatorValue(PostEventType.POST_CREATED)
    @Json
    data class PostCreatedEvent(
        @param:JsonField("post_id") val postId: UUID,
        @param:JsonField("author_id") val authorId: Long,
        @param:JsonField("text") val text: String,
        @param:JsonField("created_at") val createdAt: OffsetDateTime,
        @param:JsonField("image_keys")
        val imageKeys: JsonNullable<List<String>>
    ) : PostEvent

    @JsonDiscriminatorValue(PostEventType.POST_DELETED)
    @Json
    data class PostDeletedEvent(
        @param:JsonField("post_id") val postId: UUID,
        @param:JsonField("author_id") val authorId: Long,
    ) : PostEvent
}