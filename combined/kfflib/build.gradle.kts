plugins {
    id("kff.combined-conventions")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks {
    jar {
        manifest.attributes["FMLModType"] = "GAMELIBRARY"
    }
}
