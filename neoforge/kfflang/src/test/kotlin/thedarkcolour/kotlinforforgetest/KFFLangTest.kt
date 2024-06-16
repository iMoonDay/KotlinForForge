package thedarkcolour.kotlinforforgetest

import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Set `modLoader` in neoforge.mods.toml to
 * `"kotlinforforge"` and loaderVersion to `"[3,)"`.
 *
 * Make sure to use [MOD_CONTEXT]
 * instead of [FMLJavaModLoadingContext].
 *
 * For a more detailed example mod,
 * check out the [KotlinModdingSkeleton repository](https://github.com/thedarkcolour/KotlinModdingSkeleton).
 */
@Mod(KFFLangTest.ID)
object KFFLangTest {
    const val ID = "kfflangtest"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Hello world from Kotlin for forge Language provider!")
    }
}