import java.time.LocalDateTime

plugins {
    id("kff.neoforge-conventions")
}

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["neo_version"]}")

    // Default classpath
    api(libs.kotlin.stdlib.jdk8)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.core.jvm)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.kotlinx.serialization.json)

    implementation(projects.neoforge.kfflang)
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
                "Implementation-Timestamp" to LocalDateTime.now(),
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lib",
                "FMLModType" to "GAMELIBRARY",
            )
        }
    }
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

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kfflib-neoforge"
        }
    }
}
