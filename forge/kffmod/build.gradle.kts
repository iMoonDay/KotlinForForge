import java.time.LocalDateTime

plugins {
    id("kff.forge-conventions")
    `maven-publish`
    idea
}

val mc_version: String by project
val forge_version: String by project
val kotlin_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

minecraft {
    mappings("official", mc_version)
    copyIdeResources.set(true)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            ideaModule = "${project.parent!!.name}.${project.name}.test"

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            ideaModule = "${project.parent!!.name}.${project.name}.test"

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }
    }
}

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
    }
    api {
        minecraftLibrary.get().extendsFrom(this)
        minecraftLibrary.get().exclude("org.jetbrains", "annotations")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Default classpath
    api(kotlin("stdlib"))
    api(kotlin("stdlib-common"))
    api(kotlin("stdlib-jdk8"))
    api(kotlin("stdlib-jdk7"))
    api(kotlin("reflect"))

    implementation(project(":forge:kfflang"))
    implementation(project(":forge:kfflib"))
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
