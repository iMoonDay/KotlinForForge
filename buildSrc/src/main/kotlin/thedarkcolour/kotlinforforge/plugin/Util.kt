package thedarkcolour.kotlinforforge.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

fun alias(name: String, project: Project): String {
    val pluginDep = project.extensions.getByType<VersionCatalogsExtension>().named("libs").findPlugin(name).get().get()
    return pluginDep.pluginId
}

fun Project.getKffMaxVersion(): String {
    val kffVersion = project.version.toString()
    assert(kffVersion != "unspecified")
    return "${kffVersion.split('.')[0].toInt() + 1}.0.0"
}

fun Project.getPropertyString(key: String): String {
    return rootProject.properties[key] as String
}