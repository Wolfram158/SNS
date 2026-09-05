package ru.wolfram.postcreator.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonField
import java.util.*

@Json
data class DeletePostResponse(
    @param:JsonField("post_id") val postId: UUID,
    @param:JsonField("author_id") val authorId: Long
)