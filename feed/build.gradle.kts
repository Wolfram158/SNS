plugins {
    id("sns.application-conventions")
}

application {
    mainClass.set("ru.wolfram.feed.ApplicationKt")
}

dependencies {
    implementation("ru.tinkoff.kora:kafka")
    implementation("ru.tinkoff.kora:http-client-async")
    implementation("software.amazon.awssdk:s3:2.27.0")
}