package ru.tinkoff.kora.guide.testingintegration.ru.wolfram.subscribers

import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.wolfram.subscribers.Application

@KoraApp
interface TestApplication : Application {
    @Repository
    interface TestSubscriptionRepository : JdbcRepository {
        @Query("DELETE FROM subscriptions")
        fun deleteAll()
    }

    @Tag(TestApplication::class)
    @Root
    fun testRoot(
        ignored: TestSubscriptionRepository
    ): String = "test-root"
}