plugins {
    id("sns.application-conventions")
}

application {
    mainClass.set("ru.wolfram.subscribers.ApplicationKt")
}

ksp {
    arg("kora.app.submodule.enabled", "true")
}