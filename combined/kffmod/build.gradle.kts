plugins {
    java
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(provider {
            listOf(
                zipTree((project(":forge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
                zipTree((project(":neoforge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
            )
        })
    }
}
