package xyz.gaon.componentory.history

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidHistoryTest {
    private val commit = "a".repeat(40)
    private val fixture =
        """
        api,class,superClass,category,abstract,deprecated
        1,android.view.View,,VIEW,false,false
        1,android.widget.Old,android.view.View,VIEW,false,false
        2,android.view.View,,VIEW,false,false
        2,android.widget.New,android.view.View,VIEW,false,false
        2,android.widget.Old,android.view.View,VIEW,false,true
        3,android.view.View,,VIEW,false,false
        3,android.widget.New,android.view.View,VIEW,false,false
        4,android.view.View,,VIEW,false,false
        4,android.widget.Old,android.view.View,VIEW,false,true
    """
            .trimIndent()

    @Test
    fun transitionsKeepDeprecationSeparateFromRemovalAndSupportReintroduction() {
        val history = AndroidHistory.parse(fixture.reader(), commit)
        assertEquals(
            listOf("android.widget.New"),
            history.select(2, filter = HistoryFilter.ADDED).map { it.name },
        )
        assertEquals(
            listOf("android.widget.Old"),
            history.select(2, filter = HistoryFilter.DEPRECATED).map { it.name },
        )
        assertTrue(history.select(2).any { it.name == "android.widget.Old" })
        assertEquals(
            listOf("android.widget.Old"),
            history.select(3, filter = HistoryFilter.REMOVED).map { it.name },
        )
        assertFalse(history.select(3).any { it.name == "android.widget.Old" })
        assertEquals(
            listOf("android.widget.Old"),
            history.select(4, filter = HistoryFilter.ADDED).map { it.name },
        )
        assertEquals(1, history.introduced("android.widget.Old"))
        assertEquals("android.widget.New", history.select(2, " WIDGET.new ").single().name)
    }

    @Test
    fun parserRejectsUnverifiedOrAmbiguousRows() {
        assertThrows(IllegalArgumentException::class.java) {
            AndroidHistory.parse(fixture.reader(), "main")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidHistory.parse(
                (fixture + "\n1,android.view.View,,VIEW,false,false").reader(),
                commit,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidHistory.parse(
                fixture.replace(",VIEW,false,false", ",ADAPTER,false,false").reader(),
                commit,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidHistory.parse(fixture.replace(",false,true", ",false,maybe").reader(), commit)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AndroidHistory.parse(fixture.reader(), commit).select(37)
        }
    }

    @Test
    fun reviewedSnapshotsMatchKnownPublicIntroductionsAndDeprecation() {
        val table = File("../data/history/public-ui.csv")
        val history = table.reader().use { AndroidHistory.parse(it, commit) }
        assertEquals((1..36).toList(), history.versions)
        assertEquals(1, history.introduced("android.widget.Button"))
        assertEquals(14, history.introduced("android.widget.Switch"))
        assertEquals(21, history.introduced("android.widget.Toolbar"))
        assertFalse(history.select(13).any { it.name == "android.widget.Switch" })
        assertTrue(
            history.select(14, filter = HistoryFilter.ADDED).any {
                it.name == "android.widget.Switch"
            }
        )
        assertTrue(
            history.select(29, filter = HistoryFilter.DEPRECATED).any {
                it.name == "android.preference.Preference"
            }
        )
        assertTrue(
            history.select(36).any { it.name == "android.preference.Preference" && it.deprecated }
        )
        assertEquals(history.select(35).map { it.name }, history.select(36).map { it.name })
        assertFalse(history.select(19).any { it.name.endsWith("ArrayAdapter") })
    }
}
