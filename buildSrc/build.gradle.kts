repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net/")
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:" + libs.versions.kotlin.get())
}
