package thedarkcolour.kotlinforforge.neoforge

import net.neoforged.fml.Logging
import net.neoforged.neoforgespi.language.ILifecycleEvent
import net.neoforged.neoforgespi.language.IModInfo
import net.neoforged.neoforgespi.language.IModLanguageProvider
import net.neoforged.neoforgespi.language.ModFileScanData
import org.objectweb.asm.Type
import java.util.function.Consumer
import java.util.function.Supplier

public class KotlinLanguageProvider : IModLanguageProvider {
    override fun name(): String = "kotlinforforge"

    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer { scanData ->
            scanData.addLanguageLoader(scanData.annotations
                .filter { data -> data.annotationType == MOD_ANNOTATION }
                .associate { data ->
                    val modid = data.annotationData["value"] as String
                    val modClass = data.clazz.className

                    LOGGER.debug(Logging.SCAN, "Found @Mod class $modClass with mod id $modid")
                    modid to KotlinModTarget(modClass, modid)
                })
        }
    }

    @Suppress("removal", "DEPRECATION")
    @Deprecated(message = "Does not do anything")
    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {}

    private class KotlinModTarget(private val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {
        override fun <T> loadMod(info: IModInfo, modFileScanResults: ModFileScanData, gameLayer: ModuleLayer): T {
            return KotlinModContainer(info, className, modFileScanResults, gameLayer) as T
        }
    }

    private companion object {
        @JvmStatic
        private val MOD_ANNOTATION = Type.getType("Lnet/neoforged/fml/common/Mod;")
    }
}