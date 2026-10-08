package xyz.gaon.componentory.survivor

import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GameArtworkTest {
    @Test
    fun everyRegisteredOriginalResourceActuallyDrawsAndIsCached() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manifest =
            JSONObject(
                context.assets.open("survivor/resources.json").bufferedReader().use {
                    it.readText()
                }
            )
        val assets = GameAssets(context)
        val keys = manifest.getJSONObject("assets").keys().asSequence().toList()
        for (key in keys) {
            val bitmap = assets.bitmap(key)
            assertSame(key, bitmap, assets.bitmap(key))
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            assertTrue("Empty original artwork: " + key, pixels.any { it ushr 24 != 0 })
        }
        assertTrue(GameCatalog.characters.all { it.family.art in keys })
    }
}
