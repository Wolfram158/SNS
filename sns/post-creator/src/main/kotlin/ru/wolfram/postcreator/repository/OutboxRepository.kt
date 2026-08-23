package ru.wolfram.postcreator.repository

import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.wolfram.postcreator.dao.OutboxEventDAO

@Repository
interface OutboxRepository : R2dbcRepository {
    @Query(
        """
        INSERT INTO outbox_events (id, aggregate_type, aggregate_id, event_type, payload)
        VALUES (:event.id, :event.aggregateType, :event.aggregateId, :event.eventType, :event.payload::jsonb)
        """
    )
    suspend fun insert(event: OutboxEventDAO)
}