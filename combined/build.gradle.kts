import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kff.common-conventions")
    alias(libs.plugins.neogradle)
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
    shadow(libs.kotlin.reflect)
    shadow(libs.kotlin.stdlib)
    shadow(libs.kotlinx.coroutines.core)
    shadow(libs.kotlinx.coroutines.core.jvm)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.serialization.core)
    shadow(libs.kotlinx.serialization.json)

    // KFF Modules
    implementation(include(projects.combined.kfflang))
    implementation(include(projects.combined.kfflib))
    implementation(include(projects.combined.kffmod))
}

fun DependencyHandler.include(dep: ModuleDependency): ModuleDependency {
    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        isTransitive = false
        jarJar.pin(this, version)
        jarJar.ranged(this, "[$version,$kffMaxVersion)")
    }
    return dep
}

tasks {
    jar {
        enabled = false
    }

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

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    assemble {
        dependsOn(":combined:kfflang:build")
        dependsOn(":combined:kfflib:build")
        dependsOn(":combined:kffmod:build")
        dependsOn(jarJar)
    }
}
