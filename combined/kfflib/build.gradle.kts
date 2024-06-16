plugins {
    java
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(provider {
            listOf(
                zipTree((project(":forge:kfflib").tasks.getByName("jar") as Jar).archiveFile),
                zipTree((project(":neoforge:kfflib").tasks.getByName("jar") as Jar).archiveFile),
            )
        })

        manifest.attributes("FMLModType" to "GAMELIBRARY")
    }
}
