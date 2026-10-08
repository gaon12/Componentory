package xyz.gaon.componentory.eastereggs

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android_q.egg.quares.Quare
import com.dede.basic.globalContext
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

// No Activity, input, permission request, job scheduling, or component state change.
@RunWith(AndroidJUnit4::class)
class EasterEggResourceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun everyImportedComponentLoadsAndCatalogEntriesResolveToThePackagedPrivateBoundary() {
        val metadata =
            JSONObject(
                context.assets.open("easter-eggs/provenance.json").bufferedReader().use {
                    it.readText()
                }
            )
        assertEquals(eggSourceRevision, metadata.getString("revision"))
        val components = metadata.getJSONArray("components")
        assertEquals(65, components.length())
        val names =
            (0 until components.length())
                .map { components.getJSONObject(it).getString("className") }
                .toSet()
        names.forEach { Class.forName(it, false, context.classLoader) }
        val families = easterEggReleases.map { it.family }.distinct()
        easterEggReleases.forEach { assertTrue(it.logo.className in names) }
        families.forEach { family ->
            family.stages.forEach { assertTrue(it.className in names) }
            family.integrations.forEach { assertTrue(it.className in names) }
        }
        val info =
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.MATCH_DISABLED_COMPONENTS,
            )
        checkNotNull(info.activities)
            .filter { it.name in names }
            .forEach {
                if (it.name == "com.android_n.egg.neko.NekoLand") {
                    assertTrue(it.exported)
                    assertEquals("android.permission.BIND_QUICK_SETTINGS_TILE", it.permission)
                } else assertFalse(it.exported)
            }
        checkNotNull(info.services)
            .filter { it.name in names }
            .forEach {
                assertTrue(checkNotNull(it.permission).startsWith("android.permission.BIND_"))
            }
        val packagedNames =
            checkNotNull(info.activities).map { it.name } +
                checkNotNull(info.services).map { it.name } +
                checkNotNull(info.receivers).map { it.name }
        assertEquals(names, packagedNames.filter { it in names }.toSet())
        assertSame(context.applicationContext, globalContext.applicationContext)
    }

    @Test
    fun realNonogramRasterCluesAndPlayerMarksSurviveAnAndroidParcelRoundTrip() {
        val bitmap = Bitmap.createBitmap(5, 3, Bitmap.Config.ARGB_8888)
        try {
            for (y in 0 until 3) for (x in listOf(0, 1, 4)) bitmap.setPixel(x, y, Color.BLACK)
            val puzzle = Quare(5, 3, 1)
            puzzle.load(BitmapDrawable(context.resources, bitmap))
            assertFalse(puzzle.isBlank())
            assertArrayEquals(intArrayOf(2, 1), puzzle.getRowClue(0))
            assertArrayEquals(intArrayOf(3), puzzle.getColumnClue(4))
            assertArrayEquals(intArrayOf(0), puzzle.getColumnClue(2))
            for (y in 0 until 3) for (x in 0 until 5) puzzle.setUserMark(
                x,
                y,
                puzzle.getDataAt(x, y),
            )
            assertTrue(puzzle.check())
            puzzle.setUserMark(4, 2, 0)
            assertFalse(puzzle.check())
            assertTrue(puzzle.check(-1, 0))
            val parcel = Parcel.obtain()
            try {
                puzzle.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)
                val restored = Quare.createFromParcel(parcel)
                assertArrayEquals(intArrayOf(2, 1), restored.getRowClue(0))
                assertEquals(255, restored.getUserMark(1, 0))
                assertEquals(0, restored.getUserMark(4, 2))
                assertFalse(restored.check())
            } finally {
                parcel.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }
}
