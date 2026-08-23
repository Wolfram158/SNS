package ru.wolfram.postcreator.repository

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.postcreator.dao.PostDAO
import ru.wolfram.postcreator.dao.PostImageDAO
import java.util.*

@Repository
interface PostRepository : R2dbcRepository {
    @Query(
        """
    INSERT INTO posts (id, author_id, text) 
    VALUES (:post.id, :post.authorId, :post.text)
    """
    )
    suspend fun insert(post: PostDAO)

    @Query(
        """
        INSERT INTO post_images (id, post_id, s3_key, position)
        VALUES (:image.id, :image.postId, :image.s3Key, :image.position)
        """
    )
    suspend fun insertImage(image: PostImageDAO)

    @Query("SELECT id, text, created_at FROM posts WHERE id = :id")
    suspend fun findById(id: UUID): PostDAO?
}