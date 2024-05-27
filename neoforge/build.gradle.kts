plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    java
}

// Current KFF version
val kff_version: String by project
val kffMaxVersion = "${kff_version.split('.')[0].toInt() + 1}.0.0"
val kffGroup = "thedarkcolour"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

evaluationDependsOnChildren()

val min_mc_version: String by project
val unsupported_mc_version: String by project
val mc_version: String by project

val min_neo_version: String by project
val neo_version: String by project

val coroutines_version: String by project
val serialization_version: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

subprojects {
    java {
        withSourcesJar()
    }

    // Workaround to remove build\classes\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
    setOf(sourceSets.main, sourceSets.test)
        .map(Provider<SourceSet>::get)
        .forEach { sourceSet ->
            val mutClassesDirs = sourceSet.output.classesDirs as ConfigurableFileCollection
            val javaClassDir = sourceSet.java.classesDirectory.get()
            val mutClassesFrom = mutClassesDirs.from
                .filter {
                    val toCompare = (it as? Provider<*>)?.get()
                    return@filter javaClassDir != toCompare
                }
                .toMutableSet()
            mutClassesDirs.setFrom(mutClassesFrom)
        }
}

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
    jarJarLib(libs.kotlin.reflect)
    jarJarLib(libs.kotlin.stdlib.asProvider())
    jarJarLib(libs.kotlin.stdlib.jdk7)
    jarJarLib(libs.kotlin.stdlib.jdk8)
    jarJarLib(libs.kotlinx.coroutines.core.asProvider())
    jarJarLib(libs.kotlinx.coroutines.core.jvm)
    jarJarLib(libs.kotlinx.coroutines.jdk8)
    jarJarLib(libs.kotlinx.serialization.core)
    jarJarLib(libs.kotlinx.serialization.json)

    // KFF Modules
    api(projects.neoforge.kfflang)
    api(projects.neoforge.kfflib)
    api(projects.neoforge.kffmod)
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":neoforge", ":neoforge:kfflib", ":neoforge:kfflang", ":neoforge:kffmod")) {
        finalizedBy(project(proj).tasks.getByName("publishToMavenLocal"))
    }
}

fun DependencyHandler.jarJarLib(dependencyNotation: Provider<out ExternalModuleDependency>) {
    val dep = dependencyNotation.get().copy()
    jarJar(dep) {
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
