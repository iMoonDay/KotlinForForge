# KotlinForForge
**Instructions for other versions: [1.19.3-1.20.4](https://github.com/thedarkcolour/KotlinForForge/blob/4.x/README.md) | [1.19.2](https://github.com/thedarkcolour/KotlinForForge/blob/3.x/README.md) | [1.14-1.16.5](https://github.com/thedarkcolour/KotlinForForge/blob/1.x/README.md) | [1.17-1.17.1](https://github.com/thedarkcolour/KotlinForForge/blob/2.x/README.md)**

Makes Kotlin Forge-friendly by doing the following:
- Provides Kotlin stdlib, reflection, JSON serialization, and coroutines libraries.
- Provides `KotlinLanguageProvider` to allow usage of object declarations as @Mod targets.
- Provides `AutoKotlinEventBusSubscriber` to allow usage of object declarations as @EventBusSubscriber targets.
- Provides useful utility functions and constants

[MIGRATION GUIDE](https://gist.github.com/thedarkcolour/5590f46b0d4d8ca692add2934d05e642)

A 1.20.5 example mod is provided here: [1.20.5 KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton/tree/1.20.5-neoforge)

If you aren't sure where to start, make a fork of the KotlinModdingSkeleton repository (replace BRANCH with your version)
```git
git clone --branch BRANCH https://github.com/thedarkcolour/KotlinModdingSkeleton.git
```

To implement in an existing project, merge the following into your build script:
<details>
        <summary><b>Gradle</b></summary>

```groovy
plugins {    
    // Adds the Kotlin Gradle plugin
    id 'org.jetbrains.kotlin.jvm' version '1.9.23'
    // OPTIONAL Kotlin Serialization plugin
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.9.23'
}

repositories {
    // Add KFF Maven repository
    maven {
        name = 'Kotlin for Forge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    // Adds KFF as dependency and Kotlin libs (use the variant matching your mod loader)
    // FORGE (NOT IMPLEMENTED FOR 1.20.5)
    //implementation 'thedarkcolour:kotlinforforge:5.0.0'
    // NEOFORGE
    implementation 'thedarkcolour:kotlinforforge-neoforge:5.0.0'
}
```
</details>

<details>
        <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
plugins {
    // Adds the Kotlin Gradle plugin
    kotlin("jvm") version "1.9.23"
    // OPTIONAL Kotlin Serialization plugin
    kotlin("plugin.serialization") version "1.9.23"
}

repositories {
    // Add KFF Maven repository
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    // Adds KFF as dependency and Kotlin libs (use the variant matching your mod loader)
    // FORGE (NOT IMPLEMENTED FOR 1.20.5)
    //implementation("thedarkcolour:kotlinforforge:5.0.0")
    // NEOFORGE
    implementation("thedarkcolour:kotlinforforge-neoforge:5.0.0")
}
```
</details>

Then, change the following to your neoforge.mods.toml file:
```toml
modLoader="kotlinforforge"
# Change this if you require a certain version of KotlinForForge
loaderVersion="[5.0,)"
```

Use
```thedarkcolour.kotlinforforge.forge.MOD_CONTEXT```              
instead of ```net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext```
