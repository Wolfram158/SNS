package ru.wolfram.users.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.users.dao.RefreshTokenDAO
import java.time.OffsetDateTime

@Repository
interface RefreshTokenRepository : R2dbcRepository {

    @Query(
        """
        INSERT INTO refresh_tokens (token, user_id, expires_at, created_at)
        VALUES (:token, :userId, :expiresAt, :createdAt)
        """
    )
    suspend fun insert(
        token: String,
        userId: Long,
        expiresAt: OffsetDateTime,
        createdAt: OffsetDateTime
    ): UpdateCount

    @Query(
        """
        SELECT id, token, user_id, expires_at, created_at
        FROM refresh_tokens
        WHERE token = :token
        """
    )
    suspend fun findByToken(token: String): RefreshTokenDAO?

    @Query("DELETE FROM refresh_tokens WHERE token = :token")
    suspend fun deleteByToken(token: String): UpdateCount

    @Query("DELETE FROM refresh_tokens WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: Long): UpdateCount

    @Query("DELETE FROM refresh_tokens WHERE expires_at < :now")
    suspend fun deleteExpired(now: OffsetDateTime): UpdateCount
}