package xyz.gaon.componentory.lab

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Test

class SampleStateTest {
    @Test
    fun olderSavedPanelsRestoreWithoutTheNewRangeEnd() {
        listOf(listOf(42, "draft"), listOf(42, "draft", "Filled.Home")).forEach { bundle ->
            val state = requireNotNull(SampleState.Saver.restore(bundle))
            assertEquals(42, state.value)
            assertEquals("draft", state.text)
            assertEquals(bundle.getOrNull(2) ?: "", state.icon)
            assertEquals(80, state.rangeEnd)
        }
    }

    @Test
    fun savedPanelsKeepBothThumbsAndExistingTextAndIconValues() {
        val state = SampleState(25, "draft", "Outlined.Home", 65)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) = value is Int || value is String
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(25, restored.value)
        assertEquals(65, restored.rangeEnd)
        assertEquals("draft", restored.text)
        assertEquals("Outlined.Home", restored.icon)
    }
}
