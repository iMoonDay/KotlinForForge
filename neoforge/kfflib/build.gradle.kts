plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    idea
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["neo_version"]}")

    implementation(project(":neoforge:kfflang"))
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
