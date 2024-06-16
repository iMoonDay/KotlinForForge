import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.forgegradle)
    `maven-publish`
    idea
}

val mc_version: String by project
val forge_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

minecraft {
    mappings("official", mc_version)
    copyIdeResources.set(true)
    reobf = false

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            ideaModule = "KotlinForForge.forge.kfflang.test"

            property("forge.logging.markers", "LOADING,CORE")
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

            ideaModule = "KotlinForForge.forge.kfflang.main"

            property("forge.logging.markers", "LOADING,CORE")
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
    }
}

val nonMcLibs: Configuration by configurations.creating {
    exclude(module = "annotations")
}

repositories {
    mavenLocal()
}

dependencies {
    minecraft(libs.forge)

    configurations.getByName("api").extendsFrom(nonMcLibs)

    // Default classpath
    nonMcLibs(libs.kotlin.stdlib.jdk8)
    nonMcLibs(libs.kotlin.reflect)

    // Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lang",
                "FMLModType" to "LANGPROVIDER",
            )
        }
    }

    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

sourceSets.configureEach {
    val dir = layout.buildDirectory.dir("sourcesSets/${name}")
    output.setResourcesDir(dir)
    java.destinationDirectory = dir
}
