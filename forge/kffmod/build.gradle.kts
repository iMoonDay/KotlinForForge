import thedarkcolour.kotlinforforge.plugin.getPropertyString

plugins {
    id("kff.forge-conventions")
}

minecraft {
    mappings("official", getPropertyString("mc_version"))
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
