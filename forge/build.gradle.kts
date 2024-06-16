import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.forgegradle)
    `maven-publish`
}

// Current KFF version
val kff_version: String by project
val kffMaxVersion = "${kff_version.split('.')[0].toInt() + 1}.0.0"

val mc_version: String by project
val forge_version: String by project

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

jarJar.enable()

configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        // Include subprojects as transitive runtime dependencies
        setExtendsFrom(hashSetOf(configurations.getByName("api")))
        // Publish the jarJar
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
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
    mavenLocal()
}

dependencies {
    minecraft(libs.forge)

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
    api(projects.forge.kfflang)
    api(projects.forge.kfflib)
    api(projects.forge.kffmod)

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
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
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
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

fun DependencyHandler.minecraft(dependencyNotation: Any): Dependency? = add("minecraft", dependencyNotation)

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    dependsOn(":forge:publishToMavenLocal")
    dependsOn(":forge:kfflib:publishToMavenLocal")
    dependsOn(":forge:kfflang:publishToMavenLocal")
    dependsOn(":forge:kffmod:publishToMavenLocal")
}

fun DependencyHandler.jarJarLib(dependencyNotation: Provider<out ExternalModuleDependency>) {
    val dep = dependencyNotation.get().copy()
    jarJar("${dep.group}:${dep.name}:[${dep.version},)") {
        jarJar.pin(this, dep.version!!)
        isTransitive = false
    }
}

fun DependencyHandler.include(dep: Dependency): Dependency {
    api(dep) // Add module metadata compileOnly dependency
    jarJar(dep.copy()) {
        if (this is ModuleDependency) {
            isTransitive = false
        }
        jarJar.pin(this, version)
        jarJar.ranged(this, "[$version,$kffMaxVersion)")
    }
    return dep
}