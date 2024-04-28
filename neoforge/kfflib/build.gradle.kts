plugins {
    kotlin("jvm")
    id("net.neoforged.gradle.userdev")
    `maven-publish`
    idea
}

val neo_version: String by project
val coroutines_version: String by project
val serialization_version: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
    implementation("net.neoforged:neoforge:$neo_version")

    // Default classpath
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version)
    api("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version)

    implementation(project(":neoforge:kfflang")) {
        isTransitive = false
    }
}

tasks.withType<Jar> {
    manifest.attributes("FMLModType" to "GAMELIBRARY")
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kfflib-neoforge"
        }
    }
}
