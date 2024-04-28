plugins {
    java
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(provider {
            listOf(
                // todo forge
                //zipTree((project(":forge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
                zipTree((project(":neoforge:kffmod").tasks.getByName("jar") as Jar).archiveFile),
            )
        })
    }
}
