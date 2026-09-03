plugins {
    id("sns.application-conventions")
}

application {
    mainClass.set("ru.wolfram.postcreator.ApplicationKt")
}

dependencies {
    implementation(platform("software.amazon.awssdk:bom:2.25.60"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:netty-nio-client")
}