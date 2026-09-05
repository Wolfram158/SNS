package ru.wolfram.feed.repository

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.feed.dao.FeedItemDAO
import java.time.OffsetDateTime
import java.util.*

@Repository
interface FeedRepository : R2dbcRepository {
    @Query(
        """
        INSERT INTO feed_items (post_id, author_id, text, image_keys, created_at)
        VALUES (:postId, :authorId, :text, CAST(:imageKeys as jsonb), :createdAt)
        ON CONFLICT (post_id) DO NOTHING
    """
    )
    suspend fun save(
        postId: UUID,
        authorId: Long,
        text: String,
        imageKeys: String,
        createdAt: OffsetDateTime
    )

    @Query(
        """
        SELECT id, post_id, author_id, text, image_keys, created_at
        FROM feed_items
        WHERE author_id = ANY(:authorIds)
        ORDER BY created_at DESC, id DESC
        LIMIT :limit OFFSET :offset
    """
    )
    suspend fun findByAuthorIds(
        authorIds: List<Long>,
        offset: Int,
        limit: Int
    ): List<FeedItemDAO>

    @Query(
        """
        SELECT COUNT(*) FROM feed_items WHERE post_id = :postId
    """
    )
    suspend fun exists(postId: UUID): Long

    @Query(
        """
            DELETE FROM feed_items WHERE post_id = :postId
        """
    )
    suspend fun deleteByPostId(postId: UUID)
}