import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addToStdlib.cast

val kotlin_version: String by project
val annotations_version: String by project
val coroutines_version: String by project
val serialization_version: String by project
val max_kotlin: String by project
val max_coroutines: String by project
val max_serialization: String by project

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("net.minecraftforge.gradle")
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
kotlin.jvmToolchain {}

// Enable JarInJar
jarJar.enable()

val kotlinSourceJar by tasks.creating(Jar::class) {
    val kotlinSourceSet = kotlin.sourceSets.main.get()

    from(kotlinSourceSet.kotlin.srcDirs)
    archiveClassifier.set("sources")
}

tasks.build.get().dependsOn(kotlinSourceJar)

// Workaround to remove build\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
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

val library: Configuration by configurations.creating {
    exclude("org.jetbrains", "annotations")
}

configurations {
    api {
        extendsFrom(library)
    }

    runtimeElements {
        // Remove Minecraft from transitive maven dependencies
        exclude(group = "net.minecraftforge", module = "forge")

        // Include obf jar in the final JarJar
        outgoing {
            artifacts.clear()
            artifact(tasks.jarJar)
        }
    }
}

repositories {
    mavenCentral()
    // For testing with kfflib and making JarJar shut up
    mavenLocal()
}

dependencies {
    minecraft("net.minecraftforge:forge:1.19-41.0.91")

    fun include(group: String, name: String, version: String, maxVersion: String) {
        val lib = library(group = group, name = name, "[$version, $maxVersion)")
        jarJar(lib) {
            isTransitive = false
            jarJar.pin(lib, version)
        }
    }

    // Default classpath
    include("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", coroutines_version, max_coroutines)
    include("org.jetbrains.kotlinx", "kotlinx-serialization-json", serialization_version, max_serialization)
    // Inherited
    include("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlinx", "kotlinx-serialization-core", serialization_version, max_serialization)
    include("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version, max_kotlin)
    include("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version, max_kotlin)
}

minecraft.run {
    mappings("official", "1.19")

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }

        all {
            lazyToken("minecraft_classpath") {
                return@lazyToken library.copyRecursive().resolve()
                    .joinToString(File.pathSeparator) { it.absolutePath }
            }
        }
    }
}

tasks {
    jar {
        archiveClassifier.set("slim")
    }

    jarJar.configure {
        archiveClassifier.set("")
    }

    withType<Jar> {
        manifest {
            attributes(
                mapOf(
                    "FMLModType" to "LANGPROVIDER",
                    "Specification-Title" to "Kotlin for Forge",
                    "Automatic-Module-Name" to "kfflang",
                    "Specification-Vendor" to "Forge",
                    "Specification-Version" to "1",
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to "${project.version}",
                    "Implementation-Vendor" to "thedarkcolour",
                    "Implementation-Timestamp" to `java.text`.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .format(`java.util`.Date())
                )
            )
        }
    }

    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile>().getByName("compileKotlin") {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

fun DependencyHandler.minecraft(
    dependencyNotation: Any
): Dependency? = add("minecraft", dependencyNotation)

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            jarJar.component(this)
            artifact(kotlinSourceJar)

            // Remove Minecraft from transitive dependencies
            pom.withXml {
                asNode().get("dependencies").cast<groovy.util.NodeList>().first().cast<groovy.util.Node>().children().cast<MutableList<groovy.util.Node>>().removeAll { child ->
                    child.get("groupId").cast<groovy.util.NodeList>().first().cast<groovy.util.Node>().value() == "net.minecraftforge"
                }
            }
        }
    }
}

modrinth {
    projectId.set("ordsPcFz")
    versionNumber.set("${project.version}")
    versionType.set("release")
    gameVersions.addAll("1.18", "1.18.1", "1.19")
    loaders.add("forge")
}
