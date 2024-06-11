plugins {
    alias(libs.plugins.kotlinJvm)
    `maven-publish`
    idea
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

    compileOnly(libs.kotlin.stdlib)
    implementation(projects.neoforge.kfflib)
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kffmod-neoforge"
        }
    }
}
