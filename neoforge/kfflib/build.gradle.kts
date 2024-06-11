plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    idea
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
java.withSourcesJar()

dependencies {
    implementation(libs.neoforge)

    implementation(projects.neoforge.kfflang)
}

tasks.withType<Jar> {
    manifest.attributes("FMLModType" to "GAMELIBRARY")
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
            artifactId = "kfflib-neoforge"
        }
    }
}
