import thedarkcolour.kotlinforforge.plugin.alias

project.plugins.apply("kff.common-conventions")
project.plugins.apply(alias("neogradle", project))

project.extensions.getByType<JavaPluginExtension>().withSourcesJar()