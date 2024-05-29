import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.getByType
import thedarkcolour.kotlinforforge.plugin.alias
import thedarkcolour.kotlinforforge.plugin.getPropertyString

project.plugins.apply(JavaPlugin::class.java)
project.plugins.apply(IdeaPlugin::class.java)
project.plugins.apply(MavenPublishPlugin::class.java)
project.plugins.apply(alias("kotlinJvm", project))

project.extensions.getByType<JavaPluginExtension>().apply {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// I have no idea what I'm doing

project.version = project.properties["kff_version"] as String
project.group = "thedarkcolour"

val replacements: MutableMap<String, Any> = mutableMapOf(
    "min_mc_version" to getPropertyString("min_mc_version"),
    "unsupported_mc_version" to getPropertyString("unsupported_mc_version"),
    "min_forge_version" to getPropertyString("min_forge_version"),
    "min_neo_version" to getPropertyString("min_neo_version"),
    "kff_version" to getPropertyString("kff_version")
)
val targets = mutableListOf("META-INF/mods.toml")

project.tasks {
    withType<ProcessResources> {
        inputs.properties(replacements)

        filesMatching(targets) {
            expand(replacements)
        }
    }
}
