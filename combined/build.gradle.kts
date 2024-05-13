import net.neoforged.gradle.common.dependency.ResolvedJarJarArtifact
import org.jetbrains.kotlin.com.google.common.io.Files
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.FileSystems
import kotlin.io.path.*

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
    includeJarJar("org.jetbrains.kotlin:kotlin-stdlib-jdk7", kotlin.coreLibrariesVersion)
    includeJarJar("org.jetbrains.kotlin:kotlin-stdlib-jdk8", kotlin.coreLibrariesVersion)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm", coroutines_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8", coroutines_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm", serialization_version)
    includeJarJar("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm", serialization_version)

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
    // todo Forge
    /*
    jarJar.configure {
        manifest.attributes("FMLModType" to "LIBRARY")

        // bypass the finalizeValueOnRead

        val patchedArtifacts = ArrayList(jarJarArtifacts.resolvedArtifacts.get())

        patchedArtifacts.forEachIndexed { i, artifact ->
            if (!artifact.file.path.contains("combined")) {
                patchedArtifacts[i] = patchArtifact(artifact)
            }
        }

        jarJarArtifacts.resolvedArtifacts.set(patchedArtifacts)
    }*/

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

// Adapted from: https://github.com/object-Object/HexDebug/commit/3875f68e7220608c98d35ec60175ee9abdbad762
// adds a "FMLModType: LIBRARY" field to the manifest (does nothing on NeoForge, but fixes bug on Forge)
private fun patchArtifact(oldArtifact: ResolvedJarJarArtifact): ResolvedJarJarArtifact {
    // build/kffJarJarTmp
    val kffJarJarTmpDirectory = project.layout.buildDirectory.asFile.get().toPath().resolve("kffJarJarTmp")
    if (!kffJarJarTmpDirectory.exists()) kffJarJarTmpDirectory.createDirectories()

    // the replacement library JAR file
    val patchedLocation = kffJarJarTmpDirectory.resolve(oldArtifact.file.name)
    val patchedFile = patchedLocation.toFile()
    // the replacement artifact
    val newArtifact = ResolvedJarJarArtifact(patchedFile, oldArtifact.version, oldArtifact.versionRange, oldArtifact.group, oldArtifact.artifact)

    // don't bother patching old libs
    if (patchedFile.exists()) {
        return newArtifact
    }

    // make a copy of the library JAR file
    Files.copy(oldArtifact.file, patchedFile)
    // open the JAR as a filesystem so we can change the manifest
    val patchedFs = FileSystems.newFileSystem(patchedLocation)
    val manifestLoc = "/META-INF/MANIFEST.MF"
    val manifest = patchedFs.getPath(manifestLoc).readText()

    // add the FMLModType to the manifest text, ensuring it is on a new line
    val lf = System.lineSeparator()
    val newManifest = manifest.trimEnd() + lf + "FMLModType: LIBRARY" + lf
    // overwrite the old manifest
    patchedFs.getPath(manifestLoc).writeText(newManifest)

    return newArtifact
}
