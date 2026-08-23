package ru.wolfram.postcreator.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("posts")
data class PostDAO(
    @param:Id @param:Column("id") val id: UUID,
    @param:Column("author_id") val authorId: Long,
    @param:Column("text") val text: String,
    @param:Column("created_at") val createdAt: OffsetDateTime
)