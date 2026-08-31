plugins {
    id("sns.application-conventions")
}

application {
    mainClass.set("ru.wolfram.users.ApplicationKt")
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.85.2")

    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
}