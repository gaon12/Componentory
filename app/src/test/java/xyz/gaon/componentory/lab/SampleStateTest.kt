package xyz.gaon.componentory.lab

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SampleStateTest {
    @Test
    fun elevenFieldSavesKeepExistingInputsAndDefaultNewInlineDates() {
        val saved =
            listOf(
                4,
                "text",
                "icon",
                65,
                1705276800000L,
                Long.MIN_VALUE,
                630,
                -1,
                false,
                true,
                false,
            )
        val state = requireNotNull(SampleState.Saver.restore(saved))
        assertEquals(false, state.containerClickable)
        assertEquals(false, state.time24Hour)
        assertEquals(true, state.timeInputMode)
        assertEquals(SampleDates.INITIAL_UTC_MILLIS, state.inlineDateUtcMillis)
        assertNull(state.dateRangeStartUtcMillis)
        assertNull(state.dateRangeEndUtcMillis)
        assertEquals(false, state.dateInputMode)
        assertEquals(SampleDates.INITIAL_MONTH_UTC_MILLIS, state.dateDisplayedMonthUtcMillis)
    }

    @Test
    fun savedInlineDatesKeepEmptyPartialAndCompleteSelectionsAndOriginalEditorState() {
        val start = SampleDates.utcMillis(2024, 2, 29)
        val end = SampleDates.utcMillis(2024, 3, 2)
        listOf(null to null, start to null, start to start, start to end).forEach {
            (rangeStart, rangeEnd) ->
            val state =
                SampleState(
                    initialInlineDateUtcMillis = null,
                    initialDateRangeStartUtcMillis = rangeStart,
                    initialDateRangeEndUtcMillis = rangeEnd,
                    initialDateInputMode = true,
                    initialDateDisplayedMonthUtcMillis = SampleDates.utcMillis(2024, 3, 1),
                )
            val scope =
                object : SaverScope {
                    override fun canBeSaved(value: Any) =
                        value is Int || value is Long || value is String || value is Boolean
                }
            val saved = requireNotNull(with(SampleState.Saver) { scope.save(state) })
            val restored = requireNotNull(SampleState.Saver.restore(saved))
            assertNull(restored.inlineDateUtcMillis)
            assertEquals(rangeStart, restored.dateRangeStartUtcMillis)
            assertEquals(rangeEnd, restored.dateRangeEndUtcMillis)
            assertEquals(true, restored.dateInputMode)
            assertEquals(SampleDates.utcMillis(2024, 3, 1), restored.dateDisplayedMonthUtcMillis)
            assertEquals(SampleDates.INITIAL_UTC_MILLIS, restored.dateUtcMillis)
            assertNull(restored.dateDraftUtcMillis)
        }
    }

    @Test
    fun savedSelectedInlineDateDoesNotReplaceDialogCommittedOrDraftDates() {
        val committed = SampleDates.utcMillis(2024, 5, 10)
        val draft = SampleDates.utcMillis(2024, 5, 12)
        val inline = SampleDates.utcMillis(2024, 2, 29)
        val state =
            SampleState(
                initialDateUtcMillis = committed,
                initialDateDraftUtcMillis = draft,
                initialInlineDateUtcMillis = inline,
            )
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val saved = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(saved))
        assertEquals(inline, restored.inlineDateUtcMillis)
        assertEquals(committed, restored.dateUtcMillis)
        assertEquals(draft, restored.dateDraftUtcMillis)
    }

    @Test
    fun savedMenuChoiceAndLastUserActionUseExistingStateFields() {
        val state = SampleState(initialValue = 2, initialText = SampleMenuAction.SELECTED.name)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(2, restored.value)
        assertEquals(SampleMenuAction.SELECTED.name, restored.text)
    }

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
                assertEquals(true, state.containerClickable)
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

    @Test
    fun tenFieldSavedPanelsKeepExistingStateAndDefaultToClickableContainers() {
        val committed = SampleDates.utcMillis(2024, 1, 22)
        val draft = SampleDates.utcMillis(2024, 2, 29)
        val restored =
            requireNotNull(
                SampleState.Saver.restore(
                    listOf(7, "draft", "Filled.Home", 65, committed, draft, 1425, 5, false, true)
                )
            )
        assertEquals(7, restored.value)
        assertEquals("draft", restored.text)
        assertEquals("Filled.Home", restored.icon)
        assertEquals(65, restored.rangeEnd)
        assertEquals(committed, restored.dateUtcMillis)
        assertEquals(draft, restored.dateDraftUtcMillis)
        assertEquals(1425, restored.timeMinutes)
        assertEquals(5, restored.timeDraftMinutes)
        assertEquals(false, restored.time24Hour)
        assertEquals(true, restored.timeInputMode)
        assertEquals(true, restored.containerClickable)
    }

    @Test
    fun savedPanelsKeepChronometerAnchorAndOlderBundlesDefaultToStopped() {
        val state = SampleState(initialValue = 1, initialChronometerBaseMillis = 1_700_000_000_000)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(1, restored.value)
        assertEquals(1_700_000_000_000, restored.chronometerBaseMillis)
        // Bundles saved before the field existed restart the clock safely.
        val sixteenField =
            requireNotNull(
                SampleState.Saver.restore(
                    listOf(
                        1,
                        "",
                        "",
                        80,
                        SampleDates.INITIAL_UTC_MILLIS,
                        Long.MIN_VALUE,
                        SampleTimes.INITIAL_MINUTES,
                        -1,
                        true,
                        false,
                        true,
                        SampleDates.INITIAL_UTC_MILLIS,
                        Long.MIN_VALUE,
                        Long.MIN_VALUE,
                        false,
                        SampleDates.INITIAL_MONTH_UTC_MILLIS,
                    )
                )
            )
        assertEquals(1, sixteenField.value)
        assertEquals(0, sixteenField.chronometerBaseMillis)
    }

    @Test
    fun savedPlainContainerKeepsItsModeAndPreviousClickCount() {
        val state = SampleState(initialValue = 3, initialContainerClickable = false)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val bundle = requireNotNull(with(SampleState.Saver) { scope.save(state) })
        val restored = requireNotNull(SampleState.Saver.restore(bundle))
        assertEquals(3, restored.value)
        assertEquals(false, restored.containerClickable)
    }

    @Test
    fun namedFieldsRestoreInAnyOrderAndIgnoreUnknownFields() {
        val state =
            SampleState(
                initialValue = 7,
                initialText = "draft",
                initialIcon = "Outlined.Home",
                initialRangeEnd = 65,
                initialInlineDateUtcMillis = null,
                initialChronometerBaseMillis = 1234L,
            )
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val saved = requireNotNull(with(SampleState.Saver) { scope.save(state) }) as List<*>
        val reordered = saved.chunked(2).reversed().flatten() + listOf("futureField", 42)
        val restored = requireNotNull(SampleState.Saver.restore(reordered))
        assertEquals(7, restored.value)
        assertEquals("draft", restored.text)
        assertEquals("Outlined.Home", restored.icon)
        assertEquals(65, restored.rangeEnd)
        assertNull(restored.inlineDateUtcMillis)
        assertEquals(1234L, restored.chronometerBaseMillis)
    }

    @Test
    fun missingNamedFieldsUseDefaultsWithoutReplacingAnExplicitEmptyDate() {
        val saved = listOf("value", 4, "text", "draft")
        val restored = requireNotNull(SampleState.Saver.restore(saved))
        assertEquals(4, restored.value)
        assertEquals("draft", restored.text)
        assertEquals(SampleDates.INITIAL_UTC_MILLIS, restored.inlineDateUtcMillis)
        assertEquals(SampleTimes.INITIAL_MINUTES, restored.timeMinutes)
        assertEquals(true, restored.containerClickable)
        assertEquals(0L, restored.chronometerBaseMillis)
        val emptyDate =
            requireNotNull(SampleState.Saver.restore(saved + listOf("inlineDateUtcMillis", null)))
        assertNull(emptyDate.inlineDateUtcMillis)
    }

    @Test
    fun malformedSavesAreRejectedWithoutThrowing() {
        listOf(
                "invalid",
                emptyList<Any>(),
                listOf(1),
                listOf(1, false),
                listOf("value", 1, "text"),
                listOf("value", 1, 2, "draft"),
                listOf("value", "wrong type", "text", "draft"),
            )
            .forEach { saved -> assertNull(SampleState.Saver.restore(saved)) }
    }

    @Test
    fun completeLegacySavesKeepAllSeventeenFields() {
        val saved =
            listOf(
                7,
                "draft",
                "Filled.Home",
                65,
                100L,
                200L,
                1425,
                5,
                false,
                true,
                false,
                Long.MIN_VALUE,
                300L,
                400L,
                true,
                500L,
                600L,
            )
        val restored = requireNotNull(SampleState.Saver.restore(saved))
        assertEquals(7, restored.value)
        assertEquals("draft", restored.text)
        assertEquals("Filled.Home", restored.icon)
        assertEquals(65, restored.rangeEnd)
        assertEquals(100L, restored.dateUtcMillis)
        assertEquals(200L, restored.dateDraftUtcMillis)
        assertEquals(1425, restored.timeMinutes)
        assertEquals(5, restored.timeDraftMinutes)
        assertEquals(false, restored.time24Hour)
        assertEquals(true, restored.timeInputMode)
        assertEquals(false, restored.containerClickable)
        assertNull(restored.inlineDateUtcMillis)
        assertEquals(300L, restored.dateRangeStartUtcMillis)
        assertEquals(400L, restored.dateRangeEndUtcMillis)
        assertEquals(true, restored.dateInputMode)
        assertEquals(500L, restored.dateDisplayedMonthUtcMillis)
        assertEquals(600L, restored.chronometerBaseMillis)
    }
}
