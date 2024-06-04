import thedarkcolour.kotlinforforge.plugin.alias
import thedarkcolour.kotlinforforge.plugin.getPropertyString

project.plugins.apply("kff.common-conventions")
project.plugins.apply(alias("forgegradle", project))

project.dependencies {
    val mcVersion = getPropertyString("mc_version")
    val fgVersion = getPropertyString("forge_version")
    minecraft("net.minecraftforge:forge:$mcVersion-$fgVersion")
}

project.extensions.getByType<JavaPluginExtension>().withSourcesJar()

fun DependencyHandler.minecraft(dependency: String): Dependency? = add("minecraft", dependency)