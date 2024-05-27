plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.neogradle)
    `maven-publish`
    idea
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

runs {
    create("client") {
        modSource(sourceSets["main"])
        modSource(project(":neoforge:kfflang").sourceSets["main"])
        modSource(project(":neoforge:kfflib").sourceSets["main"])
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["neo_version"]}")

    compileOnly(libs.kotlin.stdlib)
    implementation(projects.neoforge.kfflib)
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "kffmod-neoforge"
        }
    }
}
