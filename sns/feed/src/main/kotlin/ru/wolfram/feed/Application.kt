package ru.wolfram.feed

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.database.flyway.FlywayJdbcDatabaseModule
import ru.tinkoff.kora.database.jdbc.JdbcDatabaseModule
import ru.tinkoff.kora.database.r2dbc.R2dbcDatabaseModule
import ru.tinkoff.kora.http.client.async.AsyncHttpClientModule
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.kafka.common.KafkaModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.wolfram.feed.mapper.ListLongMapperModule
import ru.wolfram.feed.s3.S3Module

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    KafkaModule,
    AsyncHttpClientModule,
    LogbackModule,
    FlywayJdbcDatabaseModule,
    JdbcDatabaseModule,
    R2dbcDatabaseModule,
    UndertowHttpServerModule,
    S3Module,
    ListLongMapperModule

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}