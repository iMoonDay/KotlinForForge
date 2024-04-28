plugins {
    kotlin("jvm")
    id("net.neoforged.gradle.userdev")
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

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

//java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

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

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    shadow("org.jetbrains.kotlin:kotlin-reflect:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlin:kotlin-stdlib:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-common:${kotlin.coreLibrariesVersion}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${coroutines_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-core:${serialization_version}")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-json:${serialization_version}")

    // KFF Modules
    implementation(include(project(":neoforge:kfflang"), kffMaxVersion))
    implementation(include(project(":neoforge:kfflib"), kffMaxVersion))
    implementation(include(project(":neoforge:kffmod"), kffMaxVersion))
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":neoforge", ":neoforge:kfflib", ":neoforge:kfflang", ":neoforge:kffmod")) {
        finalizedBy(project(proj).tasks.getByName("publishToMavenLocal"))
    }
}

fun DependencyHandler.include(dep: ModuleDependency, maxVersion: String? = null): ModuleDependency {
    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        if (maxVersion != null) {
            jarJar.ranged(this, "[$version,$maxVersion)")
        }
    }
    return dep
}

tasks {
    jarJar.configure {
        from(provider { shadow.map(::zipTree).toTypedArray() })
        manifest {
            attributes(
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge",
                "FMLModType" to "LIBRARY"
            )
        }
    }

    whenTaskAdded {
        // Disable reobfJar
        // todo: did ForgeGradle remove this task as well? If so, we can remove this snippet
        if (name == "reobfJar") {
            enabled = false
        }
        // Fight ForgeGradle and Forge crashing when MOD_CLASSES don't exist
        if (name == "prepareRuns") {
            doFirst {
                sourceSets.main.get().output.files.forEach(File::mkdirs)
            }
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
