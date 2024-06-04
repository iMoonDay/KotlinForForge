import thedarkcolour.kotlinforforge.plugin.getPropertyString
import java.time.LocalDateTime

project.plugins.apply(JavaPlugin::class)

project.version = getPropertyString("kff_version")
project.group = "thedarkcolour"

project.tasks.withType<Jar> {
    // get rid of duplicate files in the ZIP
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val subproject = project.name.substringAfterLast(':')

    from(provider {
        listOf(
            zipTree(project(":forge:$subproject").tasks.getByName("jar", Jar::class).archiveFile),
            zipTree(project(":neoforge:$subproject").tasks.getByName("jar", Jar::class).archiveFile),
        )
    })

    manifest {
        attributes(
            "Specification-Title" to "Kotlin for Forge",
            "Specification-Vendor" to "Forge",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "thedarkcolour",
            "Implementation-Timestamp" to LocalDateTime.now(),
            "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.$subproject",
        )
    }
}