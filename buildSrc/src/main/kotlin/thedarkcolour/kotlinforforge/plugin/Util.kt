package thedarkcolour.kotlinforforge.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

fun alias(name: String, project: Project): String {
    val pluginDep = project.extensions.getByType<VersionCatalogsExtension>().named("libs").findPlugin(name).get().get()
    return pluginDep.pluginId
}

fun Project.getKffMaxVersion(): String {
    val kffVersion = getPropertyString("kff_version")
    return "${kffVersion.split('.')[0].toInt() + 1}.0.0"
}

fun Project.getPropertyString(key: String): String {
    return properties[key] as String
}