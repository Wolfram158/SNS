package ru.wolfram.users.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.OffsetDateTime

@Table("users")
data class UserDAO(
    @param:Column("id") val id: Long,
    @param:Column("nickname") val nickname: String,
    @param:Column("password_hash") val passwordHash: String,
    @param:Column("created_at") val createdAt: OffsetDateTime,
    @param:Column("updated_at") val updatedAt: OffsetDateTime
)