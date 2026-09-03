package ru.wolfram.postcreator.dao

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.OffsetDateTime
import java.util.UUID

@Table("outbox_events")
data class OutboxEventDAO(
    @param:Id @param:Column("id") val id: UUID,
    @param:Column("aggregate_type") val aggregateType: String,
    @param:Column("aggregate_id") val aggregateId: UUID,
    @param:Column("event_type") val eventType: String,
    @param:Column("payload") val payload: String,
    @param:Column("created_at") val createdAt: OffsetDateTime,
    @param:Column("status") val status: String = "PENDING"
)