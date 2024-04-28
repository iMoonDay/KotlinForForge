plugins {
    java
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(provider {
            listOf(
                // todo forge
                //zipTree((project(":forge:kfflang").tasks.getByName("jar") as Jar).archiveFile),
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
