import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import thedarkcolour.kotlinforforge.plugin.getPropertyString

plugins {
    alias(libs.plugins.minotaur)
    alias(libs.plugins.cursegradle)
    alias(libs.plugins.neogradle)
    id("kff.common-conventions")
}

evaluationDependsOnChildren()

subprojects {
    tasks {
        withType<KotlinCompile> {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
            compilerOptions.freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
        }
    }
}

val supportedMcVersions = listOf("1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2")

curseforge {
    // Use the command line on Linux because IntelliJ doesn't pick up from .bashrc
    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: "no-publishing-allowed"

    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = "351264"
        releaseType = "release"
        gameVersionStrings.add("Forge")
        gameVersionStrings.add("NeoForge")
        gameVersionStrings.add("Java 17")
        gameVersionStrings.addAll(supportedMcVersions)

        // from Modrinth's Util.resolveFile
        @Suppress("DEPRECATION")
        mainArtifact(project(":combined").tasks.jarJar.get().archivePath, closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
            displayName = "Kotlin for Forge ${project.version}"
        })
    })
}

modrinth {
    projectId.set("ordsPcFz")
    versionName.set("Kotlin for Forge ${project.version}")
    versionNumber.set("${project.version}")
    versionType.set("release")
    gameVersions.addAll(supportedMcVersions)
    loaders.add("forge")
    loaders.add("neoforge")
    uploadFile.provider(project(":combined").tasks.jarJar)
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    for (proj in arrayOf(":forge", ":neoforge")) {
        finalizedBy(project(proj).tasks.getByName("publishAllMavens"))
    }
}
tasks.create("publishModPlatforms") {
    finalizedBy(tasks.create("printPublishingMessage") {
        this.doFirst {
            println("Publishing Kotlin for Forge ${getPropertyString("kff_version")} to Modrinth and CurseForge")
        }
    })
    finalizedBy(tasks.modrinth)
    finalizedBy(tasks.curseforge)
}

task<Exec>("testREADME") {
    group = "verification"
    description = "Applies steps in README to ensure it works on mdk"
    workingDir("./")
    commandLine("kotlinc", "-script", ".github/ReadmeTester.kts")
    doLast {
        executionResult.get().assertNormalExitValue()
    }
}

// Kotlin function ambiguity fix
fun <T> Property<T>.provider(value: T) {
    set(value)
}
