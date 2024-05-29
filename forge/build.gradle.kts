import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import thedarkcolour.kotlinforforge.plugin.getKffMaxVersion

plugins {
    alias(libs.plugins.forgegradle)
    id("kff.forge-conventions")
    `maven-publish`
}

val kffMaxVersion = getKffMaxVersion()

evaluationDependsOnChildren()

val mc_version: String by project
val forge_version: String by project

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

java.withSourcesJar()
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
    minecraftLibrary {
        extendsFrom(shadow)
    }
}

extensions.getByType(net.minecraftforge.gradle.userdev.UserDevExtension::class).apply {
    mappings("official", mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    shadow(libs.kotlin.reflect)
    shadow(libs.kotlin.stdlib)
    shadow(libs.kotlinx.coroutines.core)
    shadow(libs.kotlinx.coroutines.core.jvm)
    shadow(libs.kotlinx.coroutines.jdk8)
    shadow(libs.kotlinx.serialization.core)
    shadow(libs.kotlinx.serialization.json)

    // KFF Modules
    implementation(include(project(":forge:kfflang"), kffMaxVersion))
    implementation(include(project(":forge:kfflib"), kffMaxVersion))
    implementation(include(project(":forge:kffmod"), kffMaxVersion))
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
        dependsOn(jarJar)
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            suppressAllPomMetadataWarnings() // Shush
            from(components["java"])
            artifactId = "kotlinforforge"
        }
    }
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":forge", ":forge:kfflib", ":forge:kfflang", ":forge:kffmod")) {
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