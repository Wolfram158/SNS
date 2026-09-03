package ru.wolfram.postcreator.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import java.util.UUID

@Table("post_images")
data class PostImageDAO(
    @param:Id @param:Column("id") val id: UUID,
    @param:Column("post_id") val postId: UUID,
    @param:Column("s3_key") val s3Key: String,
    @param:Column("position") val position: Int
)