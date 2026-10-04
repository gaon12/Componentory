package xyz.gaon.componentory.icons

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import xyz.gaon.componentory.R

enum class IconStyle(val packageName: String, val labelRes: Int) {
    FILLED("filled", R.string.style_filled),
    OUTLINED("outlined", R.string.style_outlined),
    ROUNDED("rounded", R.string.style_rounded),
    SHARP("sharp", R.string.style_sharp),
    TWO_TONE("twotone", R.string.style_two_tone),
}

data class CatalogIcon(
    val id: String,
    val name: String,
    val style: IconStyle? = null,
    val autoMirrored: Boolean = false,
    val drawableId: Int? = null,
    val available: Boolean = true,
) {
    fun matches(query: String): Boolean {
        val term = query.trim().replace("_", "").replace(" ", "")
        return name.replace("_", "").contains(term, ignoreCase = true)
    }

    fun vector(): ImageVector {
        val receiver =
            when (style) {
                IconStyle.FILLED -> if (autoMirrored) Icons.AutoMirrored.Filled else Icons.Filled
                IconStyle.OUTLINED ->
                    if (autoMirrored) Icons.AutoMirrored.Outlined else Icons.Outlined
                IconStyle.ROUNDED -> if (autoMirrored) Icons.AutoMirrored.Rounded else Icons.Rounded
                IconStyle.SHARP -> if (autoMirrored) Icons.AutoMirrored.Sharp else Icons.Sharp
                IconStyle.TWO_TONE ->
                    if (autoMirrored) Icons.AutoMirrored.TwoTone else Icons.TwoTone
                null -> error("Framework drawables do not supply a Compose ImageVector.")
            }
        // The index comes from the exact pinned libraries, including deprecated public icons.
        val getter =
            Class.forName(id).methods.single {
                it.returnType == ImageVector::class.java &&
                    it.parameterTypes.contentEquals(arrayOf(receiver.javaClass))
            }
        return getter.invoke(null, receiver) as ImageVector
    }
}

object IconCatalog {
    const val DEFAULT_MATERIAL = "androidx.compose.material.icons.filled.AddKt"
    const val DEFAULT_PLATFORM = "android:ic_input_add"

    @Volatile private var materialCache: List<CatalogIcon>? = null

    // Public identities stay fixed for the process. Availability depends on the current resources.
    private val platformIcons: List<CatalogIcon> by lazy {
        android.R.drawable::class
            .java
            .fields
            .filter { it.type == Int::class.javaPrimitiveType }
            .map { CatalogIcon("android:${it.name}", it.name, drawableId = it.getInt(null)) }
            .sortedBy { it.name }
    }

    fun material(context: Context): List<CatalogIcon> =
        materialCache
            ?: synchronized(this) {
                materialCache
                    ?: context.assets
                        .open("material-icons.txt")
                        .bufferedReader()
                        .useLines { lines ->
                            lines
                                .map { id ->
                                    val parts = id.split('.')
                                    CatalogIcon(
                                        id = id,
                                        name = parts.last().removeSuffix("Kt"),
                                        style =
                                            IconStyle.entries.single {
                                                it.packageName == parts[parts.lastIndex - 1]
                                            },
                                        autoMirrored = "automirrored" in parts,
                                    )
                                }
                                .toList()
                        }
                        .also { materialCache = it }
            }

    fun platform(context: Context): List<CatalogIcon> =
        platformIcons.map { withCurrentAvailability(context, it) }

    fun entries(context: Context, platform: Boolean): List<CatalogIcon> =
        if (platform) platform(context) else material(context)

    fun selected(context: Context, platform: Boolean, id: String): CatalogIcon {
        if (!platform) {
            val icons = material(context)
            return icons.firstOrNull { it.id == id && it.available }
                ?: icons.first { it.id == DEFAULT_MATERIAL }
        }
        val candidate =
            platformIcons.firstOrNull { it.id == id }?.let { withCurrentAvailability(context, it) }
        if (candidate?.available == true) return candidate
        return withCurrentAvailability(context, platformIcons.first { it.id == DEFAULT_PLATFORM })
    }

    private fun withCurrentAvailability(context: Context, icon: CatalogIcon): CatalogIcon =
        icon.copy(
            available =
                runCatching { context.getDrawable(requireNotNull(icon.drawableId)) }.getOrNull() !=
                    null
        )
}
