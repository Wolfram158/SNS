package ru.wolfram.postcreator.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonField
import java.time.OffsetDateTime
import java.util.UUID

@Json
data class CreatePostResponse(
    @param:JsonField("id") val id: UUID,
    @param:JsonField("author_id") val authorId: Long,
    @param:JsonField("text") val text: String,
    @param:JsonField("created_at") val createdAt: OffsetDateTime,
    @param:JsonField("image_keys") val imageKeys: List<String>
)