package xyz.gaon.componentory.compare

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SampleDates
import xyz.gaon.componentory.lab.SampleState

class SampleSetupTest {
    @Test
    fun standaloneTimesCopyMidnightNoonAndDayEndWithTheirFormatOnly() {
        listOf(LabComponent.TIME_PICKER, LabComponent.TIME_INPUT).forEach { component ->
            val families =
                DesignFamily.entries.filter { it.unsupportedReason(component, API) == null }
            listOf(0, 720, 1439).forEach { minutes ->
                listOf(false, true).forEach { format ->
                    val source =
                        SampleState(
                            initialValue = 4,
                            initialText = "uncommitted editor text",
                            initialTimeMinutes = minutes,
                            initialTime24Hour = format,
                            initialTimeDraftMinutes = 77,
                            initialTimeInputMode = true,
                            initialDateDraftUtcMillis = SampleDates.utcMillis(2025, 12, 25),
                        )
                    families.forEach { sourceFamily ->
                        val captured = SampleSetup.capture(component, sourceFamily, source, API)
                        assertEquals(
                            setOf("component", "family", "time", "time24Hour"),
                            captured.savedValues().keys,
                        )
                        val restored = requireNotNull(SampleSetup.restore(captured.savedValues()))
                        families.forEach { targetFamily ->
                            val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                            assertNotSame(source, target)
                            assertEquals(minutes, target.timeMinutes)
                            assertEquals(format, target.time24Hour)
                            assertEquals(component.initialValue, target.value)
                            assertEquals("", target.text)
                            assertNull(target.timeDraftMinutes)
                            assertFalse(target.timeInputMode)
                            assertNull(target.dateDraftUtcMillis)
                            target.timeMinutes = (minutes + 1) % 1440
                            target.time24Hour = !format
                            assertEquals(minutes, source.timeMinutes)
                            assertEquals(format, source.time24Hour)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun standaloneTimeCopiesRejectUnsupportedDirectionsAndSanitizeRestoredFields() {
        listOf(LabComponent.TIME_PICKER, LabComponent.TIME_INPUT).forEach { component ->
            DesignFamily.entries.forEach { family ->
                val restored =
                    requireNotNull(
                        SampleSetup.restore(
                            mapOf(
                                "component" to component.name,
                                "family" to family.name,
                                "time" to 0,
                                "time24Hour" to false,
                                "value" to 2,
                                "text" to "unrelated observation",
                                "icon" to "unrelated icon",
                                "date" to 10L,
                                "containerClickable" to false,
                            )
                        )
                    )
                if (family.unsupportedReason(component, API) != null) {
                    assertEquals(setOf("component", "family"), restored.savedValues().keys)
                    val result = restored.copyTo(DesignFamily.MATERIAL3, API)
                    assertNull(result.state)
                    assertEquals(SetupCopyReason.SOURCE_UNSUPPORTED, result.reason)
                } else {
                    assertEquals(
                        setOf("component", "family", "time", "time24Hour"),
                        restored.savedValues().keys,
                    )
                    DesignFamily.entries
                        .filter { it.unsupportedReason(component, API) != null }
                        .forEach { target ->
                            val result = restored.copyTo(target, API)
                            assertNull(result.state)
                            assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                        }
                    val copy = requireNotNull(restored.copyTo(DesignFamily.MATERIAL3, API).state)
                    assertEquals(0, copy.timeMinutes)
                    assertFalse(copy.time24Hour)
                    assertEquals(0, copy.value)
                    assertEquals("", copy.text)
                    assertEquals("", copy.icon)
                    assertTrue(copy.containerClickable)
                }
            }
        }
    }

    @Test
    fun checkedTextConfigurationCopiesUncheckedAndCheckedWithoutUnrelatedHistory() {
        val families = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(0, 1).forEach { checked ->
            val source =
                SampleState(
                    initialValue = checked,
                    initialText = "unrelated result",
                    initialDateDraftUtcMillis = SampleDates.utcMillis(2025, 6, 1),
                    initialTimeDraftMinutes = 90,
                )
            families.forEach { sourceFamily ->
                val setup =
                    SampleSetup.capture(LabComponent.CHECKED_TEXT_VIEW, sourceFamily, source, API)
                assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
                val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                families.forEach { targetFamily ->
                    val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                    assertNotSame(source, target)
                    assertEquals(checked, target.value)
                    assertEquals("", target.text)
                    assertNull(target.dateDraftUtcMillis)
                    assertNull(target.timeDraftMinutes)
                    target.value = 1 - checked
                    assertEquals(checked, source.value)
                }
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { targetFamily ->
                    val result = restored.copyTo(targetFamily, API)
                    assertNull(result.state)
                    assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                }
            }
        }
        val unsupported =
            SampleSetup.capture(
                LabComponent.CHECKED_TEXT_VIEW,
                DesignFamily.MATERIAL3,
                SampleState(1, "hidden"),
                API,
            )
        assertEquals(setOf("component", "family"), unsupported.savedValues().keys)
        assertEquals(
            SetupCopyReason.SOURCE_UNSUPPORTED,
            unsupported.copyTo(DesignFamily.CLASSIC, API).reason,
        )
    }

    @Test
    fun fixedTextFixturesHaveNoTransferablePerPanelInputs() {
        DesignFamily.entries.forEach { family ->
            val setup =
                SampleSetup.capture(LabComponent.TEXT, family, SampleState(7, "unrelated"), API)
            assertEquals(setOf("component", "family"), setup.savedValues().keys)
            val result = setup.copyTo(family, API)
            assertNull(result.state)
            assertEquals(SetupCopyReason.NO_INPUTS, result.reason)
        }
    }

    @Test
    fun inlineDateCopiesAcrossGenuineProvidersWithoutObservedActionsOrEditorMode() {
        val date = SampleDates.utcMillis(2025, 2, 28)
        val state =
            SampleState(
                initialValue = 19,
                initialDateDraftUtcMillis = SampleDates.utcMillis(2024, 12, 25),
                initialInlineDateUtcMillis = date,
                initialDateInputMode = true,
                initialDateDisplayedMonthUtcMillis = SampleDates.utcMillis(2026, 6, 1),
            )
        DesignFamily.entries
            .filter { it != DesignFamily.MATERIAL2 }
            .forEach { source ->
                val setup = SampleSetup.capture(LabComponent.DATE_PICKER, source, state, API)
                DesignFamily.entries
                    .filter { it != DesignFamily.MATERIAL2 }
                    .forEach { destination ->
                        val target = requireNotNull(setup.copyTo(destination, API).state)
                        assertEquals(date, target.inlineDateUtcMillis)
                        assertEquals(0, target.value)
                        assertNull(target.dateDraftUtcMillis)
                        assertFalse(target.dateInputMode)
                        assertEquals(
                            SampleDates.utcMillis(2025, 2, 1),
                            target.dateDisplayedMonthUtcMillis,
                        )
                        assertNotSame(state, target)
                    }
            }
        val calendar =
            SampleSetup.capture(LabComponent.CALENDAR_VIEW, DesignFamily.CLASSIC, state, API)
        assertEquals(
            date,
            requireNotNull(calendar.copyTo(DesignFamily.HOLO, API).state).inlineDateUtcMillis,
        )
        assertEquals(
            SetupCopyReason.TARGET_UNSUPPORTED,
            calendar.copyTo(DesignFamily.MATERIAL3, API).reason,
        )
    }

    @Test
    fun emptyInlineDateIsEligibleAndSurvivesSavingButCannotReplaceANativeDate() {
        val setup =
            SampleSetup.capture(
                LabComponent.DATE_PICKER,
                DesignFamily.MATERIAL3,
                SampleState(initialInlineDateUtcMillis = null),
                API,
            )
        assertTrue(setup.savedValues()["inlineDatePresent"] == true)
        assertFalse(setup.savedValues().containsKey("inlineDate"))
        val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
        val target = requireNotNull(restored.copyTo(DesignFamily.MATERIAL3, API).state)
        assertNull(target.inlineDateUtcMillis)
        assertEquals(SampleDates.INITIAL_MONTH_UTC_MILLIS, target.dateDisplayedMonthUtcMillis)
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            val result = restored.copyTo(family, API)
            assertNull(result.state)
            assertEquals(SetupCopyReason.DATE_REQUIRED, result.reason)
        }
        assertEquals(
            SetupCopyReason.TARGET_UNSUPPORTED,
            restored.copyTo(DesignFamily.MATERIAL2, API).reason,
        )
    }

    @Test
    fun rangeSelectionCopiesEmptyPartialSameDayAndCompleteEndpointsWithoutDrafts() {
        val start = SampleDates.utcMillis(2025, 2, 28)
        val end = SampleDates.utcMillis(2025, 3, 2)
        listOf(null to null, start to null, start to start, start to end).forEach {
            (rangeStart, rangeEnd) ->
            val source =
                SampleState(
                    initialValue = 7,
                    initialDateRangeStartUtcMillis = rangeStart,
                    initialDateRangeEndUtcMillis = rangeEnd,
                    initialDateInputMode = true,
                    initialDateDisplayedMonthUtcMillis = SampleDates.utcMillis(2026, 6, 1),
                )
            val setup =
                SampleSetup.capture(
                    LabComponent.DATE_RANGE_PICKER,
                    DesignFamily.MATERIAL3,
                    source,
                    API,
                )
            assertTrue(setup.savedValues()["dateRangePresent"] == true)
            val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
            val target = requireNotNull(restored.copyTo(DesignFamily.MATERIAL3, API).state)
            assertEquals(rangeStart, target.dateRangeStartUtcMillis)
            assertEquals(rangeEnd, target.dateRangeEndUtcMillis)
            assertFalse(target.dateInputMode)
            assertEquals(0, target.value)
            assertEquals(
                if (rangeStart == null) SampleDates.INITIAL_MONTH_UTC_MILLIS
                else SampleDates.utcMillis(2025, 2, 1),
                target.dateDisplayedMonthUtcMillis,
            )
            target.dateRangeStartUtcMillis = null
            assertEquals(rangeStart, source.dateRangeStartUtcMillis)
            assertEquals(
                SetupCopyReason.TARGET_UNSUPPORTED,
                restored.copyTo(DesignFamily.CLASSIC, API).reason,
            )
        }
    }

    @Test
    fun ineligibleOrUnsupportedSnapshotsNeverSaveNewInlineDatePayloads() {
        val source =
            SampleState(
                initialInlineDateUtcMillis = null,
                initialDateRangeStartUtcMillis = SampleDates.INITIAL_UTC_MILLIS,
            )
        listOf(LabComponent.BUTTON, LabComponent.DATE_PICKER, LabComponent.DATE_RANGE_PICKER)
            .forEach { component ->
                val setup = SampleSetup.capture(component, DesignFamily.MATERIAL2, source, API)
                assertNull(setup.inlineDate)
                assertNull(setup.dateRange)
                assertFalse(setup.savedValues().containsKey("inlineDatePresent"))
                assertFalse(setup.savedValues().containsKey("dateRangePresent"))
                assertNull(setup.copyTo(DesignFamily.MATERIAL3, API).state)
            }
    }

    @Test
    fun boundSelectionsAndKnobsBecomeIndependentFreshTargetInputs() {
        val inputs =
            mapOf(
                LabComponent.CHECKBOX to 1,
                LabComponent.SWITCH to 0,
                LabComponent.TOGGLE_BUTTON to 1,
                LabComponent.RADIO to 2,
                LabComponent.TRI_STATE_CHECKBOX to 2,
                LabComponent.ICON_TOGGLE to 0,
                LabComponent.FILLED_ICON_TOGGLE to 1,
                LabComponent.TONAL_ICON_TOGGLE to 1,
                LabComponent.OUTLINED_ICON_TOGGLE to 0,
                LabComponent.FILTER_CHIP to 1,
                LabComponent.ELEVATED_FILTER_CHIP to 0,
                LabComponent.INPUT_CHIP to 1,
                LabComponent.SINGLE_SEGMENTED to 3,
                LabComponent.MULTI_SEGMENTED to 5,
                LabComponent.SPINNER to 2,
                LabComponent.RATING to 4,
                LabComponent.NUMBER_PICKER to 9,
                LabComponent.SLIDER to 0,
                LabComponent.PROGRESS to 70,
                LabComponent.CIRCULAR_PROGRESS to 30,
                LabComponent.BADGE to 0,
                LabComponent.BADGED_BOX to 12,
                LabComponent.VIDEO_VIEW to 1,
                LabComponent.SHARE_ACTION_PROVIDER to 2,
                LabComponent.EDGE_EFFECT to 3,
                LabComponent.NAVIGATION_BAR to 2,
                LabComponent.NAVIGATION_RAIL to 3,
                LabComponent.TAB_ROW to 3,
                LabComponent.SCROLLABLE_TAB_ROW to 8,
                LabComponent.PRIMARY_TAB_ROW to 2,
                LabComponent.SECONDARY_TAB_ROW to 2,
                LabComponent.PRIMARY_SCROLLABLE_TAB_ROW to 5,
                LabComponent.SECONDARY_SCROLLABLE_TAB_ROW to 5,
                LabComponent.SNACKBAR to 4,
                LabComponent.TOP_APP_BAR to 2,
                LabComponent.BOTTOM_APP_BAR to 3,
                LabComponent.MODAL_NAVIGATION_DRAWER to 1,
                LabComponent.DISMISSIBLE_NAVIGATION_DRAWER to 1,
                LabComponent.BOTTOM_DRAWER to 1,
                LabComponent.BOTTOM_SHEET_SCAFFOLD to 1,
                LabComponent.MODAL_BOTTOM_SHEET to 1,
                LabComponent.BACKDROP_SCAFFOLD to 1,
                LabComponent.SWIPE_TO_DISMISS to 1,
            )
        inputs.forEach { (component, value) ->
            val family = supportedFamily(component)
            val source = SampleState(initialValue = value, initialText = "old action")
            val setup = SampleSetup.capture(component, family, source, API)
            val target = requireNotNull(setup.copyTo(family, API).state)
            assertNotSame(source, target)
            assertEquals(component.name, value, target.value)
            assertEquals("", target.text)
            target.value = component.initialValue
            assertEquals(value, source.value)
            assertEquals("old action", source.text)
        }
    }

    @Test
    fun safeTextIncludingEmptyTextCopiesWithoutSubmissionCounts() {
        listOf(
                LabComponent.TEXT_FIELD,
                LabComponent.OUTLINED_TEXT_FIELD,
                LabComponent.AUTOCOMPLETE,
                LabComponent.MULTI_AUTOCOMPLETE,
                LabComponent.SEARCH_VIEW,
                LabComponent.DIALER_FILTER,
                LabComponent.EXPOSED_DROPDOWN,
            )
            .forEach { component ->
                listOf("", "Input 日本語").forEach { input ->
                    val family = supportedFamily(component)
                    val setup = SampleSetup.capture(component, family, SampleState(8, input), API)
                    val target = requireNotNull(setup.copyTo(family, API).state)
                    assertEquals(input, target.text)
                    assertEquals(component.initialValue, target.value)
                }
            }
    }

    @Test
    fun resultsSecureContentAndStaticPreviewsHaveNoTransferableInput() {
        listOf(
                LabComponent.BUTTON,
                LabComponent.OUTLINED_BUTTON,
                LabComponent.CHIP,
                LabComponent.ASSIST_CHIP,
                LabComponent.SUGGESTION_CHIP,
                LabComponent.DIALOG,
                LabComponent.POPUP_MENU,
                LabComponent.SECURE_TEXT_FIELD,
                LabComponent.OUTLINED_SECURE_TEXT_FIELD,
                LabComponent.HORIZONTAL_DIVIDER,
                LabComponent.DOT_BADGE,
                LabComponent.INDETERMINATE_LINEAR_PROGRESS,
                LabComponent.WEB_VIEW,
                LabComponent.QUICK_CONTACT_BADGE,
                LabComponent.PERMANENT_NAVIGATION_DRAWER,
                LabComponent.LIST_ITEM,
                LabComponent.PLAIN_TOOLTIP,
                LabComponent.RICH_TOOLTIP,
            )
            .forEach { component ->
                val family = supportedFamily(component)
                val setup =
                    SampleSetup.capture(
                        component,
                        family,
                        SampleState(19, "sensitive or observed"),
                        API,
                    )
                assertNull(setup.value)
                assertNull(setup.text)
                assertFalse(setup.savedValues().containsKey("value"))
                assertFalse(setup.savedValues().containsKey("text"))
                val result = setup.copyTo(family, API)
                assertNull(result.state)
                assertEquals(component.name, SetupCopyReason.NO_INPUTS, result.reason)
            }
    }

    @Test
    fun bothRangeThumbsAndPlainContainerModeAreEligibleInputs() {
        val range =
            SampleSetup.capture(
                LabComponent.RANGE_SLIDER,
                DesignFamily.MATERIAL2,
                SampleState(initialValue = 15, initialRangeEnd = 65),
                API,
            )
        val targetRange = requireNotNull(range.copyTo(DesignFamily.MATERIAL3, API).state)
        assertEquals(15, targetRange.value)
        assertEquals(65, targetRange.rangeEnd)
        val container =
            SampleSetup.capture(
                LabComponent.CARD,
                DesignFamily.MATERIAL2,
                SampleState(initialValue = 9, initialContainerClickable = false),
                API,
            )
        val targetContainer = requireNotNull(container.copyTo(DesignFamily.MATERIAL3, API).state)
        assertFalse(targetContainer.containerClickable)
        assertEquals(0, targetContainer.value)
    }

    @Test
    fun committedDateAndTimeCopyWithoutActionsDraftsOrEditorMode() {
        val committedDate = SampleDates.utcMillis(2024, 2, 29)
        val date =
            SampleSetup.capture(
                LabComponent.DATE_PICKER_DIALOG,
                DesignFamily.MATERIAL3,
                SampleState(
                    initialValue = 1,
                    initialDateUtcMillis = committedDate,
                    initialDateDraftUtcMillis = SampleDates.utcMillis(2025, 7, 4),
                ),
                API,
            )
        val targetDate = requireNotNull(date.copyTo(DesignFamily.CLASSIC, API).state)
        assertEquals(committedDate, targetDate.dateUtcMillis)
        assertEquals(0, targetDate.value)
        assertNull(targetDate.dateDraftUtcMillis)
        val time =
            SampleSetup.capture(
                LabComponent.TIME_PICKER_DIALOG,
                DesignFamily.MATERIAL3,
                SampleState(
                    initialValue = 2,
                    initialTimeMinutes = 1425,
                    initialTimeDraftMinutes = 5,
                    initialTime24Hour = false,
                    initialTimeInputMode = true,
                ),
                API,
            )
        val targetTime = requireNotNull(time.copyTo(DesignFamily.HOLO, API).state)
        assertEquals(1425, targetTime.timeMinutes)
        assertFalse(targetTime.time24Hour)
        assertNull(targetTime.timeDraftMinutes)
        assertFalse(targetTime.timeInputMode)
        assertEquals(0, targetTime.value)
    }

    @Test
    fun unsupportedProvidersNeverReceiveHiddenInputs() {
        val source = SampleState(42, "hidden", "hidden icon", 90)
        val unsupported =
            SampleSetup.capture(
                LabComponent.RANGE_SLIDER,
                DesignFamily.CLASSIC,
                source,
                API,
                "hidden icon",
            )
        assertEquals(setOf("component", "family"), unsupported.savedValues().keys)
        assertEquals(
            SetupCopyReason.SOURCE_UNSUPPORTED,
            unsupported.copyTo(DesignFamily.MATERIAL3, API).reason,
        )
        val supported =
            SampleSetup.capture(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL3, source, API)
        assertEquals(
            SetupCopyReason.TARGET_UNSUPPORTED,
            supported.copyTo(DesignFamily.CLASSIC, API).reason,
        )
        // The lower API is an availability-policy input, not an execution environment for this app.
        val number =
            SampleSetup.capture(LabComponent.NUMBER_PICKER, DesignFamily.CLASSIC, source, API)
        assertEquals(
            SetupCopyReason.SOURCE_UNSUPPORTED,
            number.copyTo(DesignFamily.HOLO, 10).reason,
        )
    }

    @Test
    fun exactDisplayedIconsCopyWithinTheirCatalogAndMismatchesAreNoOps() {
        val materialId = "androidx.compose.material.icons.automirrored.outlined.ArrowBackKt"
        val source = SampleState(initialIcon = "")
        val material =
            SampleSetup.capture(LabComponent.ICON, DesignFamily.MATERIAL2, source, API, materialId)
        assertEquals(
            materialId,
            requireNotNull(material.copyTo(DesignFamily.MATERIAL3, API, true).state).icon,
        )
        val mismatch = material.copyTo(DesignFamily.CLASSIC, API, true, "android:ic_input_add")
        assertNull(mismatch.state)
        assertEquals(SetupCopyReason.ICON_UNAVAILABLE, mismatch.reason)
        val unavailable = material.copyTo(DesignFamily.MATERIAL3, API, false)
        assertNull(unavailable.state)
        assertEquals(SetupCopyReason.ICON_UNAVAILABLE, unavailable.reason)
        val native =
            SampleSetup.capture(
                LabComponent.ICON,
                DesignFamily.CLASSIC,
                source,
                API,
                "android:ic_menu_camera",
            )
        assertEquals(
            "android:ic_menu_camera",
            requireNotNull(native.copyTo(DesignFamily.HOLO, API, true).state).icon,
        )
        assertEquals("", source.icon)
        val actionWithIcon =
            SampleSetup.capture(
                LabComponent.FAB,
                DesignFamily.MATERIAL3,
                SampleState(8),
                API,
                materialId,
            )
        val target = requireNotNull(actionWithIcon.copyTo(DesignFamily.MATERIAL2, API, true).state)
        assertEquals(materialId, target.icon)
        assertEquals(0, target.value)
    }

    @Test
    fun unavailableIconCanBeOmittedWhileOtherInputsStartAFreshTarget() {
        // Synthetic availability input: today's compound icon samples share the same catalog.
        val setup =
            SampleSetup.capture(
                LabComponent.BADGED_BOX,
                DesignFamily.MATERIAL2,
                SampleState(17, "observed action"),
                API,
                "androidx.compose.material.icons.filled.HomeKt",
            )
        val retainedIcon = "androidx.compose.material.icons.outlined.FavoriteKt"
        val result = setup.copyTo(DesignFamily.MATERIAL3, API, false, retainedIcon)
        val target = requireNotNull(result.state)
        assertTrue(result.iconSkipped)
        assertEquals(17, target.value)
        assertEquals(retainedIcon, target.icon)
        assertEquals("", target.text)
    }

    @Test
    fun saveableEntryContainsOnlySanitizedInputsAndKeepsDisabledConfiguration() {
        val setup =
            SampleSetup.capture(
                LabComponent.TIME_PICKER_DIALOG,
                DesignFamily.MATERIAL3,
                SampleState(
                    initialValue = 1,
                    initialText = "action",
                    initialTimeMinutes = 65,
                    initialTimeDraftMinutes = 700,
                    initialTime24Hour = false,
                    initialTimeInputMode = true,
                ),
                API,
            )
        val entry = ComparisonEntry(setup, false)
        val scope =
            object : SaverScope {
                override fun canBeSaved(value: Any) =
                    value is Int || value is Long || value is String || value is Boolean
            }
        val saved = requireNotNull(with(ComparisonEntry.Saver) { scope.save(entry) })
        val restored = requireNotNull(ComparisonEntry.Saver.restore(saved))
        assertFalse(restored.enabled)
        assertEquals(LabComponent.TIME_PICKER_DIALOG, restored.setup.component)
        assertEquals(DesignFamily.MATERIAL3, restored.setup.sourceFamily)
        val target = requireNotNull(restored.setup.copyTo(DesignFamily.CLASSIC, API).state)
        assertEquals(65, target.timeMinutes)
        assertFalse(target.time24Hour)
        assertNull(target.timeDraftMinutes)
        assertFalse(target.timeInputMode)
        assertEquals(0, target.value)
        assertEquals("", target.text)
        assertNull(ComparisonEntry.Saver.restore(emptyList<Any>()))
    }

    @Test
    fun restoringAnEntryReappliesTheWhitelistAndRejectsUnknownIdentities() {
        listOf(LabComponent.POPUP_MENU, LabComponent.SECURE_TEXT_FIELD).forEach { component ->
            val restored =
                requireNotNull(
                    SampleSetup.restore(
                        mapOf(
                            "component" to component.name,
                            "family" to supportedFamily(component).name,
                            "value" to 2,
                            "text" to "secret or result",
                            "icon" to "unrelated",
                            "date" to 10L,
                            "time" to 123,
                            "containerClickable" to false,
                        )
                    )
                )
            assertEquals(setOf("component", "family"), restored.savedValues().keys)
            assertEquals(
                SetupCopyReason.NO_INPUTS,
                restored.copyTo(restored.sourceFamily, API).reason,
            )
        }
        assertNull(SampleSetup.restore(mapOf("component" to "UNKNOWN", "family" to "CLASSIC")))
        assertNull(SampleSetup.restore(mapOf("component" to "BUTTON", "family" to "UNKNOWN")))
        assertNotNull(SampleSetup.restore(mapOf("component" to "CHECKBOX", "family" to "CLASSIC")))
    }

    @Test
    fun clockCopiesCarryOnlyTheFormatOrRunningStateTheirPanelActuallyOwns() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        // The text clock's only input is its pinned format choice.
        val clockSource = SampleState(initialTime24Hour = false, initialText = "ignored")
        platform.forEach { sourceFamily ->
            val setup = SampleSetup.capture(LabComponent.TEXT_CLOCK, sourceFamily, clockSource, API)
            assertEquals(setOf("component", "family", "time24Hour"), setup.savedValues().keys)
            val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
            platform.forEach { targetFamily ->
                val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                assertNotSame(clockSource, target)
                assertFalse(target.time24Hour)
                assertEquals("", target.text)
                assertEquals(LabComponent.TEXT_CLOCK.initialValue, target.value)
            }
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { targetFamily ->
                val result = restored.copyTo(targetFamily, API)
                assertNull(result.state)
                assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
            }
        }
    }

    @Test
    fun chronometerCopiesRunningFlagAndAnchorWithoutUnrelatedState() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(
                SampleState(initialValue = 0, initialChronometerBaseMillis = 90_000),
                SampleState(initialValue = 1, initialChronometerBaseMillis = 1_700_000_000_000),
            )
            .forEach { source ->
                platform.forEach { sourceFamily ->
                    val setup =
                        SampleSetup.capture(LabComponent.CHRONOMETER, sourceFamily, source, API)
                    assertEquals(
                        setOf("component", "family", "value", "chronometerBase"),
                        setup.savedValues().keys,
                    )
                    val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                    platform.forEach { targetFamily ->
                        val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                        assertNotSame(source, target)
                        assertEquals(source.value, target.value)
                        assertEquals(source.chronometerBaseMillis, target.chronometerBaseMillis)
                        assertEquals("", target.text)
                    }
                }
            }
        // Purely visual clocks, scroll containers and layouts own no copyable inputs.
        listOf(
                LabComponent.ANALOG_CLOCK,
                LabComponent.DIGITAL_CLOCK,
                LabComponent.SCROLL_VIEW,
                LabComponent.HORIZONTAL_SCROLL_VIEW,
                LabComponent.FRAME_LAYOUT,
                LabComponent.LINEAR_LAYOUT,
                LabComponent.TABLE_LAYOUT,
                LabComponent.GRID_LAYOUT,
                LabComponent.RELATIVE_LAYOUT,
                LabComponent.SPACE,
                LabComponent.ABSOLUTE_LAYOUT,
                LabComponent.TWO_LINE_LIST_ITEM,
            )
            .forEach { component ->
                val captured =
                    SampleSetup.capture(component, DesignFamily.CLASSIC, SampleState(), API)
                assertEquals(setOf("component", "family"), captured.savedValues().keys)
                val result = captured.copyTo(DesignFamily.HOLO, API)
                assertNull(result.state)
                assertEquals(SetupCopyReason.NO_INPUTS, result.reason)
            }
    }

    @Test
    fun listCopiesCarryOnlyTheCheckedOrExpandedValue() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(
                LabComponent.LIST_VIEW to 3,
                LabComponent.GRID_VIEW to 8,
                LabComponent.EXPANDABLE_LIST_VIEW to 5,
            )
            .forEach { (component, selection) ->
                val source = SampleState(initialValue = selection, initialText = "ignored")
                platform.forEach { sourceFamily ->
                    val setup = SampleSetup.capture(component, sourceFamily, source, API)
                    assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
                    val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                    platform.forEach { targetFamily ->
                        val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                        assertNotSame(source, target)
                        assertEquals(selection, target.value)
                        assertEquals("", target.text)
                    }
                    listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                        val result = restored.copyTo(family, API)
                        assertNull(result.state)
                        assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                    }
                }
            }
    }

    @Test
    fun zoomCopiesCarryOnlyTheZoomLevel() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(
                LabComponent.ZOOM_CONTROLS,
                LabComponent.ZOOM_BUTTON,
                LabComponent.ZOOM_BUTTONS_CONTROLLER,
            )
            .forEach { component ->
                val source = SampleState(initialValue = 8, initialText = "ignored")
                platform.forEach { sourceFamily ->
                    val setup = SampleSetup.capture(component, sourceFamily, source, API)
                    assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
                    val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                    platform.forEach { targetFamily ->
                        val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                        assertNotSame(source, target)
                        assertEquals(8, target.value)
                        assertEquals("", target.text)
                    }
                    listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                        val result = restored.copyTo(family, API)
                        assertNull(result.state)
                        assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                    }
                }
            }
    }

    @Test
    fun switcherCopiesCarryOnlyTheDisplayedChildIndex() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(
                LabComponent.VIEW_ANIMATOR to 3,
                LabComponent.VIEW_SWITCHER to 1,
                LabComponent.VIEW_FLIPPER to 2,
                LabComponent.TEXT_SWITCHER to 2,
                LabComponent.IMAGE_SWITCHER to 1,
                LabComponent.ADAPTER_VIEW_FLIPPER to 3,
                LabComponent.STACK_VIEW to 5,
                LabComponent.TAB_HOST to 2,
                LabComponent.GALLERY to 4,
                LabComponent.SLIDING_DRAWER to 1,
                LabComponent.POPUP_WINDOW to 3,
                LabComponent.LIST_POPUP_WINDOW to 2,
                LabComponent.TOOLBAR to 3,
                LabComponent.ACTION_MENU_VIEW to 2,
            )
            .forEach { (component, index) ->
                val source = SampleState(initialValue = index, initialText = "ignored")
                platform.forEach { sourceFamily ->
                    val setup = SampleSetup.capture(component, sourceFamily, source, API)
                    assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
                    val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                    platform.forEach { targetFamily ->
                        val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                        assertNotSame(source, target)
                        assertEquals(index, target.value)
                        assertEquals("", target.text)
                    }
                    listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                        val result = restored.copyTo(family, API)
                        assertNull(result.state)
                        assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                    }
                }
            }
    }

    @Test
    fun transientCopiesCarryOnlyTheOpenCount() {
        val platform = listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        listOf(LabComponent.PLAIN_DIALOG, LabComponent.PROGRESS_DIALOG, LabComponent.TOAST)
            .forEach { component ->
                val source = SampleState(initialValue = 2, initialText = "ignored")
                platform.forEach { sourceFamily ->
                    val setup = SampleSetup.capture(component, sourceFamily, source, API)
                    assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
                    val restored = requireNotNull(SampleSetup.restore(setup.savedValues()))
                    platform.forEach { targetFamily ->
                        val target = requireNotNull(restored.copyTo(targetFamily, API).state)
                        assertNotSame(source, target)
                        assertEquals(2, target.value)
                        assertEquals("", target.text)
                    }
                    listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                        val result = restored.copyTo(family, API)
                        assertNull(result.state)
                        assertEquals(SetupCopyReason.TARGET_UNSUPPORTED, result.reason)
                    }
                }
            }
    }

    private fun supportedFamily(component: LabComponent) =
        DesignFamily.entries.first { it.unsupportedReason(component, API) == null }

    private companion object {
        const val API = 36
    }
}
