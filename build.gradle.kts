import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.neogradle)
    alias(libs.plugins.kotlinJvm)
}

// Current KFF version
val kff_version: String by project
val kffGroup = "thedarkcolour"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

allprojects {
    version = kff_version
    group = kffGroup
}

val min_mc_version: String by project
val unsupported_mc_version: String by project
val kff_max_version: String by project

val replacements = mutableMapOf(
    "min_mc_version" to min_mc_version,
    "unsupported_mc_version" to unsupported_mc_version,
    "kff_version" to kff_version,
    "kff_max_version" to kff_max_version,
)
val targets = mutableListOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")

subprojects {
    tasks {
        withType<ProcessResources> {
            inputs.properties(replacements)

            filesMatching(targets) {
                expand(replacements)
            }
        }

        withType<KotlinCompile> {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
            compilerOptions.freeCompilerArgs.set(listOf("-Xexplicit-api=warning", "-Xjvm-default=all"))
        }
    }
}

// maven.repo.local is set within the Julia script in the website branch
tasks.create("publishAllMavens") {
    dependsOn(":forge:publishAllMavens")
    dependsOn(":neoforge:publishAllMavens")
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
