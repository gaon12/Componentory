package xyz.gaon.componentory.icons

import android.content.res.Configuration
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig

@RunWith(AndroidJUnit4::class)
class IconCatalogResourceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun frameworkCatalogRetainsEveryPublicResourceAndItsAvailability() {
        val expected =
            android.R.drawable::class
                .java
                .fields
                .filter { it.type == Int::class.javaPrimitiveType }
                .associate { it.name to it.getInt(null) }
        val start = SystemClock.elapsedRealtimeNanos()
        val icons = IconCatalog.platform(context)
        logTiming("framework catalog", start, icons.size)

        assertEquals(expected.keys.sorted(), icons.map { it.name })
        assertEquals(icons.size, icons.map { it.id }.toSet().size)
        icons.forEach { icon ->
            assertEquals("android:${icon.name}", icon.id)
            assertEquals(expected[icon.name], icon.drawableId)
            assertNull(icon.style)
            assertFalse(icon.autoMirrored)
            val loadable =
                runCatching { context.getDrawable(requireNotNull(icon.drawableId)) }.getOrNull() !=
                    null
            // Public resources missing on a vendor OS must remain explicit catalog entries.
            assertEquals("Availability of ${icon.id}", loadable, icon.available)
        }
        Log.i(
            "IconCatalogResourceTest",
            "Framework resources: ${icons.size}; unavailable: ${icons.count { !it.available }}",
        )
    }

    @Test
    fun materialCatalogMatchesEveryIdentityAndStyleInThePinnedAsset() {
        val ids = context.assets.open("material-icons.txt").bufferedReader().use { it.readLines() }
        val icons = IconCatalog.material(context)
        assertEquals("1.7.8", BuildConfig.MATERIAL_ICONS_VERSION)
        assertEquals(11_385, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(ids, icons.map { it.id })

        val identity =
            Regex(
                "androidx\\.compose\\.material\\.icons\\.(automirrored\\.)?" +
                    "(filled|outlined|rounded|sharp|twotone)\\.([A-Za-z0-9_]+)Kt"
            )
        icons.forEach { icon ->
            val match = identity.matchEntire(icon.id)
            assertNotNull("Invalid pinned icon identity: ${icon.id}", match)
            val groups = requireNotNull(match).groupValues
            assertEquals(groups[3], icon.name)
            assertEquals(groups[2], requireNotNull(icon.style).packageName)
            assertEquals(groups[1].isNotEmpty(), icon.autoMirrored)
            assertNull(icon.drawableId)
            assertTrue(icon.available)
        }
        IconStyle.entries.forEach { style ->
            assertEquals(2_277, icons.count { it.style == style })
            assertTrue(icons.any { it.style == style && it.autoMirrored })
            assertTrue(icons.any { it.style == style && !it.autoMirrored })
        }
    }

    @Test
    fun selectedLookupsMatchCatalogEntriesAndKeepFallbacksWithinTheirSource() {
        listOf(true, false).forEach { platform ->
            val icons = IconCatalog.entries(context, platform)
            val available = if (platform) icons.filter { it.available } else icons
            assertTrue("The source needs a usable default icon", available.isNotEmpty())
            val examples =
                listOf(available.first(), available[available.size / 2], available.last())
                    .distinctBy { it.id }
            val default =
                if (platform) IconCatalog.DEFAULT_PLATFORM else IconCatalog.DEFAULT_MATERIAL
            val fallback = available.single { it.id == default }
            val otherSource =
                if (platform) IconCatalog.DEFAULT_MATERIAL else IconCatalog.DEFAULT_PLATFORM
            val start = SystemClock.elapsedRealtimeNanos()
            examples.forEach { icon ->
                assertEquals(icon, IconCatalog.selected(context, platform, icon.id))
            }
            listOf("", "missing-icon-resource", otherSource).forEach { id ->
                assertEquals(fallback, IconCatalog.selected(context, platform, id))
            }
            val unavailable = icons.firstOrNull { !it.available }
            if (unavailable != null)
                assertEquals(fallback, IconCatalog.selected(context, platform, unavailable.id))
            val lookupCount = examples.size + 3 + if (unavailable == null) 0 else 1
            if (platform) logTiming("framework selected lookups", start, lookupCount)
        }
    }

    @Test
    fun frameworkCatalogAndSelectionRespectThemedAndConfigurationContexts() {
        val original = Configuration(context.resources.configuration)
        val configuration =
            Configuration(original).apply {
                densityDpi = if (original.densityDpi == 160) 240 else 160
                uiMode =
                    (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        Configuration.UI_MODE_NIGHT_YES
                setLocale(Locale.JAPANESE)
            }
        val configured = context.createConfigurationContext(configuration)
        val contexts =
            listOf(
                ContextThemeWrapper(context, android.R.style.Theme_Light),
                ContextThemeWrapper(context, android.R.style.Theme_Holo_Light),
                ContextThemeWrapper(context, android.R.style.Theme_Material_Light),
                ContextThemeWrapper(configured, android.R.style.Theme_Material_Light),
            )
        val identities = IconCatalog.platform(context).map { it.id }
        contexts.forEach { resourceContext ->
            val icons = IconCatalog.platform(resourceContext)
            assertEquals(identities, icons.map { it.id })
            icons.forEach { icon ->
                val loadable =
                    runCatching { resourceContext.getDrawable(requireNotNull(icon.drawableId)) }
                        .getOrNull() != null
                assertEquals(
                    "Current resource availability of ${icon.id}",
                    loadable,
                    icon.available,
                )
            }
            val available = icons.first { it.available && it.id != IconCatalog.DEFAULT_PLATFORM }
            assertEquals(available, IconCatalog.selected(resourceContext, true, available.id))
            val fallback = icons.single { it.id == IconCatalog.DEFAULT_PLATFORM }
            assertEquals(
                fallback,
                IconCatalog.selected(resourceContext, true, "missing-icon-resource"),
            )
            icons
                .firstOrNull { !it.available }
                ?.let { unavailable ->
                    assertEquals(
                        fallback,
                        IconCatalog.selected(resourceContext, true, unavailable.id),
                    )
                }
        }
        assertEquals(
            "Resource contexts must not change the app configuration",
            original,
            context.resources.configuration,
        )
    }

    private fun logTiming(operation: String, start: Long, count: Int) {
        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000.0
        // These resource timings do not measure rendering or interaction performance.
        val message = "$operation: $elapsedMs ms; items: $count"
        Log.i("IconCatalogResourceTest", message)
        InstrumentationRegistry.getInstrumentation()
            .sendStatus(
                2,
                Bundle().apply { putString("stream", "IconCatalogResourceTest: $message\n") },
            )
    }
}
