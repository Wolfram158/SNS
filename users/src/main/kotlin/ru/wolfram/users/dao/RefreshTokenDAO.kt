package ru.wolfram.users.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.OffsetDateTime

@Table("refresh_tokens")
data class RefreshTokenDAO(
    @param:Column("id") val id: Long,
    @param:Column("token") val token: String,
    @param:Column("user_id") val userId: Long,
    @param:Column("expires_at") val expiresAt: OffsetDateTime,
    @param:Column("created_at") val createdAt: OffsetDateTime
)