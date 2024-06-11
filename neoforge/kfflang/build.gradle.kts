plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
java.withSourcesJar()

repositories {
    maven("https://maven.neoforged.net/releases")
    // use mojang libraries without NeoGradle
    maven("https://repo.minebench.de/")
}

dependencies {
    implementation(libs.fancymodloader)

    // Default classpath
    api(libs.kotlin.stdlib)
    api(libs.kotlin.stdlib.jdk7)
    api(libs.kotlin.stdlib.jdk8)
    api(libs.kotlin.reflect)
}

tasks.withType<Jar> {
    manifest.attributes(
        "FMLModType" to "LIBRARY",
        // Required for language providers
        "Implementation-Version" to version
    )
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kfflang-neoforge"
        }
    }
}
