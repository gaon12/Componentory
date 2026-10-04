package xyz.gaon.componentory.lab

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SampleStateTest {
    @Test
    fun olderSavedPanelsRestoreWithDefaultDateState() {
        listOf(
                listOf(42, "draft"),
                listOf(42, "draft", "Filled.Home"),
                listOf(42, "draft", "Filled.Home", 65),
            )
            .forEach { bundle ->
                val state = requireNotNull(SampleState.Saver.restore(bundle))
                assertEquals(42, state.value)
                assertEquals("draft", state.text)
                assertEquals(bundle.getOrNull(2) ?: "", state.icon)
                assertEquals(bundle.getOrNull(3) ?: 80, state.rangeEnd)
                assertEquals(SampleDates.INITIAL_UTC_MILLIS, state.dateUtcMillis)
                assertNull(state.dateDraftUtcMillis)
            }
    }

    @Test
    fun savedPanelsKeepBothThumbsAndExistingTextAndIconValues() {
        val state = SampleState(25, "draft", "Outlined.Home", 65)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(25, restored.value)
        assertEquals(65, restored.rangeEnd)
        assertEquals("draft", restored.text)
        assertEquals("Outlined.Home", restored.icon)
        assertNull(restored.dateDraftUtcMillis)
        assertNull(restored.timeDraftMinutes)
    }

    @Test
    fun fiveFieldSavedPanelsKeepCommittedDateWithoutAnUnconfirmedDraft() {
        val date = SampleDates.utcMillis(2024, 2, 29)
        val restored = requireNotNull(SampleState.Saver.restore(listOf(2, "", "", 80, date)))
        assertEquals(2, restored.value)
        assertEquals(date, restored.dateUtcMillis)
        assertNull(restored.dateDraftUtcMillis)
    }

    @Test
    fun savedPanelsKeepCommittedDateAndUnconfirmedDraftSeparately() {
        val committed = SampleDates.utcMillis(2024, 1, 22)
        val draft = SampleDates.utcMillis(2024, 2, 29)
        val state =
            SampleState(
                initialValue = 1,
                initialDateUtcMillis = committed,
                initialDateDraftUtcMillis = draft,
            )
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(1, restored.value)
        assertEquals(committed, restored.dateUtcMillis)
        assertEquals(draft, restored.dateDraftUtcMillis)
    }

    @Test
    fun sixFieldSavedPanelsKeepTheirDatesAndDefaultTheNewTimeState() {
        val committed = SampleDates.utcMillis(2024, 1, 22)
        val draft = SampleDates.utcMillis(2024, 2, 29)
        val restored =
            requireNotNull(SampleState.Saver.restore(listOf(1, "", "", 80, committed, draft)))
        assertEquals(committed, restored.dateUtcMillis)
        assertEquals(draft, restored.dateDraftUtcMillis)
        assertEquals(630, restored.timeMinutes)
        assertNull(restored.timeDraftMinutes)
        assertEquals(true, restored.time24Hour)
        assertEquals(false, restored.timeInputMode)
    }

    @Test
    fun savedTimeStateKeepsNondefaultCommittedDraftFormatAndInputMode() {
        val state =
            SampleState(
                initialValue = 1,
                initialTimeMinutes = 1425,
                initialTimeDraftMinutes = 5,
                initialTime24Hour = false,
                initialTimeInputMode = true,
            )
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(1, restored.value)
        assertEquals(1425, restored.timeMinutes)
        assertEquals(5, restored.timeDraftMinutes)
        assertEquals(false, restored.time24Hour)
        assertEquals(true, restored.timeInputMode)
    }
}
