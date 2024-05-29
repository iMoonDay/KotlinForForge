pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
    }
    plugins {
        id ("org.gradle.toolchains.foojay-resolver-convention") version ("0.5.0")
    }
}
include("forge", "neoforge", "combined")
include("forge:kfflang",    "forge:kfflib",    "forge:kffmod"   )
include("neoforge:kfflang", "neoforge:kfflib", "neoforge:kffmod")
include("combined:kfflang", "combined:kfflib", "combined:kffmod")

rootProject.name = "KotlinForForge"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
