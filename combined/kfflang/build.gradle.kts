plugins {
    java
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(provider {
            listOf(
                zipTree((project(":forge:kfflang").tasks.getByName("jar") as Jar).archiveFile),
                zipTree((project(":neoforge:kfflang").tasks.getByName("jar") as Jar).archiveFile),
            )
        })

        manifest.attributes(
            "FMLModType" to "LIBRARY",
            // Required for language providers
            "Implementation-Version" to version
        )
    }
}
