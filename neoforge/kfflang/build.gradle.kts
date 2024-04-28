import net.neoforged.gradle.dsl.common.extensions.RunnableSourceSet

plugins {
    kotlin("jvm")
    `maven-publish`
    idea
    java
    id("net.neoforged.gradle.userdev")
}

val neo_version: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

// Tells NeoGradle to treat this source set as a separate mod
sourceSets["test"].extensions.getByType<RunnableSourceSet>().configure { runnable -> runnable.modIdentifier("kfflangtest") }

val nonmclibs: Configuration by configurations.creating {
}

runs {
    configureEach {
        modSource(sourceSets["main"])
        modSource(sourceSets["test"])
        dependencies {
            runtime((nonmclibs))
        }
    }
    create("client")
    create("server") {
        programArgument("--nogui")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neo_version")

    configurations.getByName("api").extendsFrom(nonmclibs)

    // Default classpath
    nonmclibs(kotlin("stdlib"))
    nonmclibs(kotlin("stdlib-common"))
    nonmclibs(kotlin("stdlib-jdk8"))
    nonmclibs(kotlin("stdlib-jdk7"))
    nonmclibs(kotlin("reflect"))
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
