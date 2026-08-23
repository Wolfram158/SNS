plugins {
    id("sns.kora-conventions")
    application
}

application {
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "-XX:+UseZGC",
        "-XX:+ZGenerational"
    )
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}