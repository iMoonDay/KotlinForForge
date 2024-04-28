import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("net.neoforged.gradle.userdev")
    `maven-publish`
}

val kff_version: String by project
val kffMaxVersion = "${kff_version.split(".")[0].toInt() + 1}.0.0"
val kffGroup = "thedarkcolour"

val coroutines_version: String by project
val serialization_version: String by project

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

base {
    archivesName.set("kotlinforforge")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
    }
}

repositories {
    mavenCentral()
}

jarJar.enable()

dependencies {
    includeJarJar("org.jetbrains.kotlin:kotlin-reflect", kotlin.coreLibrariesVersion)
    includeJarJar("org.jetbrains.kotlin:kotlin-stdlib", kotlin.coreLibrariesVersion)
    includeJarJar("org.jetbrains.kotlin:kotlin-stdlib-common", kotlin.coreLibrariesVersion)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-coroutines-core", coroutines_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm", coroutines_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8", coroutines_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-serialization-core", serialization_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-serialization-json", serialization_version)

    // KFF Modules
    implementation(include(project(":combined:kfflang"), kffMaxVersion))
    implementation(include(project(":combined:kfflib"), kffMaxVersion))
    implementation(include(project(":combined:kffmod"), kffMaxVersion))
}

fun DependencyHandler.includeJarJar(dependency: String, version: String) {
    jarJar("$dependency:[$version,)") { version { prefer(version) } }
}

fun DependencyHandler.include(dep: Dependency, maxVersion: String? = null): Dependency {
    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        if (this is ModuleDependency) {
            isTransitive = false
        }
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
        manifest.attributes("FMLModType" to "LIBRARY")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    assemble {
        dependsOn(":combined:kfflang:build")
        dependsOn(":combined:kfflib:build")
        dependsOn(":combined:kffmod:build")
        dependsOn(jarJar)
    }
}
