package ru.wolfram.users.repository

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.users.dao.UserDAO
import java.time.OffsetDateTime

@Repository
interface UserRepository : R2dbcRepository {
    @Query(
        """
        SELECT id, nickname, password_hash, created_at, updated_at
        FROM users
        WHERE id = :id
        """
    )
    suspend fun findById(id: Long): UserDAO?

    @Query(
        """
        SELECT id, nickname, password_hash, created_at, updated_at
        FROM users
        WHERE nickname = :nickname
        """
    )
    suspend fun findByNickname(nickname: String): UserDAO?

    @Query(
        """
        INSERT INTO users (nickname, password_hash, created_at, updated_at)
        VALUES (:nickname, :passwordHash, :createdAt, :updatedAt)
        RETURNING id, nickname, password_hash, created_at, updated_at
        """
    )
    suspend fun insert(
        nickname: String,
        passwordHash: String,
        createdAt: OffsetDateTime,
        updatedAt: OffsetDateTime
    ): UserDAO
}