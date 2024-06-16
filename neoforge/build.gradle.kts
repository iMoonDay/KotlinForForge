plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

jarJar.enable()

// Only publish "-all" variant
configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        // Publish the jarJar ONLY
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    includeJarJar(libs.kotlin.reflect)
    includeJarJar(libs.kotlin.stdlib.asProvider())
    includeJarJar(libs.kotlin.stdlib.jdk7)
    includeJarJar(libs.kotlin.stdlib.jdk8)
    includeJarJar(libs.kotlinx.coroutines.core.asProvider())
    includeJarJar(libs.kotlinx.coroutines.core.jvm)
    includeJarJar(libs.kotlinx.coroutines.jdk8)
    includeJarJar(libs.kotlinx.serialization.core)
    includeJarJar(libs.kotlinx.serialization.json)

    // KFF Modules
    api(projects.neoforge.kfflang)
    api(projects.neoforge.kfflib)
    api(projects.neoforge.kffmod)
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    dependsOn(":neoforge:publishToMavenLocal")
    dependsOn(":neoforge:kfflib:publishToMavenLocal")
    dependsOn(":neoforge:kfflang:publishToMavenLocal")
    dependsOn(":neoforge:kffmod:publishToMavenLocal")
}

fun DependencyHandler.includeJarJar(dependencyNotation: Provider<out ExternalModuleDependency>) {
    val dep = dependencyNotation.get().copy()
    jarJar("${dep.group}:${dep.name}:[${dep.version},)") {
        version {
            prefer(dep.version!!)
        }
    }
}

tasks {
    jarJar.configure {
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    assemble {
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kotlinforforge-neoforge"
        }
    }
}
