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

    runs {
        runs {
            create("client") {
                workingDirectory(project.file("run"))

                ideaModule = "${project.parent!!.name}.${project.name}.test"

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "debug")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }
                }

                mods {
                    create("kfflibtest") {
                        source(sourceSets.test.get())
                    }
                }
            }


            create("server") {
                workingDirectory(project.file("run/server"))

                ideaModule = "${project.parent!!.name}.${project.name}.test"

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "debug")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }
                }

                mods {
                    create("kfflibtest") {
                        source(sourceSets.test.get())
                    }
                }
            }

            create("gameTestServer") {
                workingDirectory(project.file("run/server"))

                ideaModule = "${project.parent!!.name}.${project.name}.test"

                property("forge.logging.markers", "SCAN,LOADING,CORE")
                property("forge.logging.console.level", "warn")
                property("forge.enabledGameTestNamespaces", "kfflibtest")

                mods {
                    create("kfflib") {
                        source(sourceSets.main.get())
                    }

                    create("kfflibtest") {
                        source(sourceSets.test.get())
                    }
                }

            }
        }
    }
}

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
    }
    api {
        minecraftLibrary.get().extendsFrom(this)
        minecraftLibrary.get().exclude("org.jetbrains", "annotations")
    }
}

repositories {
    mavenLocal()
}

dependencies {
    minecraft(libs.forge)

    implementation(projects.forge.kfflang)

    // Hack fix for now, force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
}

tasks {
    withType<Jar> {
        manifest.attributes("FMLModType" to "GAMELIBRARY")
    }
    
    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
