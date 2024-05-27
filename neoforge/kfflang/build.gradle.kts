import net.neoforged.gradle.dsl.common.extensions.RunnableSourceSet

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    idea
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

// Tells NeoGradle to treat this source set as a separate mod
sourceSets["test"].extensions.getByType<RunnableSourceSet>().configure { runnable -> runnable.modIdentifier("kfflangtest") }

val nonMcLibs: Configuration by configurations.creating {
    exclude(module = "annotations")
}

runs {
    configureEach {
        modSource(sourceSets["main"])
        modSource(sourceSets["test"])
        dependencies {
            runtime(nonMcLibs)
        }
    }
    create("client")
    create("server") {
        programArgument("--nogui")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["neo_version"]}")

    configurations.getByName("api").extendsFrom(nonMcLibs)

    // Default classpath
    nonMcLibs(libs.kotlin.stdlib)
    nonMcLibs(libs.kotlin.stdlib.jdk7)
    nonMcLibs(libs.kotlin.stdlib.jdk8)
    nonMcLibs(libs.kotlin.reflect)
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
