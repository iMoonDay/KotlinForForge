plugins {
    id("kff.neoforge-conventions")
}

runs {
    create("client") {
        modSource(sourceSets["main"])
        modSource(project(":neoforge:kfflang").sourceSets["main"])
        modSource(project(":neoforge:kfflib").sourceSets["main"])
    }
}

dependencies {
    implementation(libs.neoforge)

    // Default classpath
    api(libs.kotlin.stdlib.jdk8)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.core.jvm)
    api(libs.kotlinx.coroutines.jdk8)
    api(libs.kotlinx.serialization.json)

    implementation(projects.neoforge.kfflang) {
        isTransitive = false
    }
    implementation(projects.neoforge.kfflib) {
        isTransitive = false
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
            artifactId = "kffmod-neoforge"
        }
    }
}
