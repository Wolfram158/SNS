object Versions {
    const val kora = "1.2.18"
    const val coroutines = "1.8.1"
    const val reactor = "3.8.6"
    const val r2dbcPostgresql = "1.1.2.RELEASE"
    const val postgresqlJdbc = "42.7.13"
    const val flyway = "9.22.3"
    const val junit = "5.10.0"
}

object Deps {
    object Kora {
        const val parent = "ru.tinkoff.kora:kora-parent:${Versions.kora}"
        const val symbolProcessors = "ru.tinkoff.kora:symbol-processors:${Versions.kora}"
        const val databaseFlyway = "ru.tinkoff.kora:database-flyway:${Versions.kora}"
        const val databaseR2dbc = "ru.tinkoff.kora:database-r2dbc:${Versions.kora}"
        const val databaseJdbc = "ru.tinkoff.kora:database-jdbc:${Versions.kora}"
        const val httpServerUndertow = "ru.tinkoff.kora:http-server-undertow:${Versions.kora}"
        const val jsonModule = "ru.tinkoff.kora:json-module:${Versions.kora}"
        const val configHocon = "ru.tinkoff.kora:config-hocon:${Versions.kora}"
        const val loggingLogback = "ru.tinkoff.kora:logging-logback:${Versions.kora}"
        const val testJunit5 = "ru.tinkoff.kora:test-junit5:${Versions.kora}"
    }

    object Coroutines {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val reactor = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.coroutines}"
    }

    const val reactorCore = "io.projectreactor:reactor-core:${Versions.reactor}"
    const val r2dbcPostgresql = "org.postgresql:r2dbc-postgresql:${Versions.r2dbcPostgresql}"
    const val postgresqlJdbc = "org.postgresql:postgresql:${Versions.postgresqlJdbc}"
    const val flywayCore = "org.flywaydb:flyway-core:${Versions.flyway}"
    const val junitBom = "org.junit:junit-bom:${Versions.junit}"
    const val junitJupiter = "org.junit.jupiter:junit-jupiter"
}