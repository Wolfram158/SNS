plugins {
    id("sns.kotlin-conventions")
    id("com.google.devtools.ksp")
}

val koraBom = configurations.create("koraBom")

configurations {
    named("ksp").get().extendsFrom(koraBom)
    named("compileOnly").get().extendsFrom(koraBom)
    named("runtimeOnly").get().extendsFrom(koraBom)
    named("implementation").get().extendsFrom(koraBom)
    named("testCompileOnly").get().extendsFrom(koraBom)
    named("kspTest").get().extendsFrom(koraBom)
    named("testRuntimeOnly").get().extendsFrom(koraBom)
    named("testImplementation").get().extendsFrom(koraBom)
}

dependencies {
    "koraBom"(platform(Deps.Kora.parent))
    "ksp"(Deps.Kora.symbolProcessors)

    "implementation"(Deps.Kora.databaseFlyway)
    "implementation"(Deps.Kora.databaseR2dbc)
    "implementation"(Deps.Kora.databaseJdbc)
    "implementation"(Deps.Kora.httpServerUndertow)
    "implementation"(Deps.Kora.jsonModule)
    "implementation"(Deps.Kora.configHocon)
    "implementation"(Deps.Kora.loggingLogback)

    "implementation"(Deps.Coroutines.core)
    "implementation"(Deps.Coroutines.reactor)
    "implementation"(Deps.reactorCore)

    "implementation"(Deps.r2dbcPostgresql)
    "runtimeOnly"(Deps.postgresqlJdbc)
    "implementation"(Deps.flywayCore)

    "kspTest"(Deps.Kora.symbolProcessors)
    "testImplementation"(Deps.flywayCore)
    "testImplementation"(Deps.Kora.databaseFlyway)
    "testImplementation"(Deps.Kora.databaseR2dbc)
    "testImplementation"(Deps.Kora.databaseJdbc)
    "testRuntimeOnly"(Deps.postgresqlJdbc)
    "testImplementation"(platform(Deps.junitBom))
    "testImplementation"(Deps.junitJupiter)
    "testImplementation"(Deps.Kora.testJunit5)
    "testImplementation"(Deps.mockk)
    "testImplementation"("org.testcontainers:junit-jupiter:1.21.4")
    "testImplementation"("org.testcontainers:postgresql:1.21.4")
}

kotlin {
    sourceSets["main"].kotlin.srcDir("build/generated/ksp/main/kotlin")
    sourceSets["test"].kotlin.srcDir("build/generated/ksp/test/kotlin")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    filter {
        excludeTestsMatching("*$*")
    }
}