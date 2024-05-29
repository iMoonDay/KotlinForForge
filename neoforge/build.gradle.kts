import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kff.neoforge-conventions")
    `maven-publish`
}

// Current KFF version
val kff_version: String by project
val kffMaxVersion = "${kff_version.split('.')[0].toInt() + 1}.0.0"

evaluationDependsOnChildren()

val shadow: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

jarJar.enable()

// Only publish "-all" variant
configurations {
    apiElements {
        artifacts.clear()
    }
    runtimeElements {
        setExtendsFrom(emptySet())
        // Publish the jarJar ONLY
        artifacts.clear()
        outgoing.artifact(tasks.jarJar)
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
    implementation(include(projects.neoforge.kfflang))
    implementation(include(projects.neoforge.kfflib))
    implementation(include(projects.neoforge.kffmod))
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":neoforge", ":neoforge:kfflib", ":neoforge:kfflang", ":neoforge:kffmod")) {
        finalizedBy(project(proj).tasks.getByName("publishToMavenLocal"))
    }
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
