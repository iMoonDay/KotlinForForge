plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.forgegradle)
    `maven-publish`
    idea
}

val mc_version: String by project
val forge_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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
    mavenLocal()
}

dependencies {
    minecraft(libs.forge)

    compileOnly(libs.kotlin.stdlib)
    implementation(projects.forge.kfflib)

    // Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
