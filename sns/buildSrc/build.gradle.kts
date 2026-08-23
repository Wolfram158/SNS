plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")

    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.25-1.0.20")
}

kotlin {
    jvmToolchain(21)
}