package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentAvailabilityTest {
    @Test
    fun textSamplesUseActualSuppliersAndKeepCheckedTextFrameworkOnly() {
        DesignFamily.entries.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TEXT, 24))
            assertEquals(
                when (family) {
                    DesignFamily.MATERIAL2 -> "androidx.compose.material.Text"
                    DesignFamily.MATERIAL3 -> "androidx.compose.material3.Text"
                    else -> "android.widget.TextView"
                },
                family.source(LabComponent.TEXT),
            )
            if (family.platform != null) {
                assertNull(family.unsupportedReason(LabComponent.CHECKED_TEXT_VIEW, 24))
                assertEquals(
                    "android.widget.CheckedTextView",
                    family.source(LabComponent.CHECKED_TEXT_VIEW),
                )
            } else {
                assertNotNull(family.unsupportedReason(LabComponent.CHECKED_TEXT_VIEW, 36))
                assertEquals("Not provided", family.source(LabComponent.CHECKED_TEXT_VIEW))
            }
        }
        assertEquals(1, LabComponent.TEXT.minimumApi)
        assertEquals(1, LabComponent.CHECKED_TEXT_VIEW.minimumApi)
        assertEquals(0, LabComponent.CHECKED_TEXT_VIEW.initialValue)
        assertEquals(ComponentCategory.CONTENT, LabComponent.TEXT.category)
        assertEquals(ComponentCategory.CONTENT, LabComponent.CHECKED_TEXT_VIEW.category)
    }

    @Test
    fun textSearchFindsRealClassNamesWithoutInventingCheckedTextLibrarySources() {
        listOf(
                "android.widget.TextView",
                "androidx.compose.material.Text",
                "androidx.compose.material3.Text",
            )
            .forEach { source -> assertTrue(LabComponent.TEXT.matchesSearch(source)) }
        assertTrue(LabComponent.CHECKED_TEXT_VIEW.matchesSearch("android.widget.CheckedTextView"))
        assertFalse(
            LabComponent.CHECKED_TEXT_VIEW.matchesSearch(
                "androidx.compose.material.CheckedTextView"
            )
        )
        assertFalse(
            LabComponent.CHECKED_TEXT_VIEW.matchesSearch(
                "androidx.compose.material3.CheckedTextView"
            )
        )
        assertFalse(LabComponent.TEXT.matchesSearch("android.widget.CheckedTextView"))
    }

    @Test
    fun inlineDatePickersUseOnlyTheirGenuineSuppliers() {
        val platformFamilies =
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
        platformFamilies.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.DATE_PICKER, 24))
            assertNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 24))
            assertNotNull(family.unsupportedReason(LabComponent.DATE_RANGE_PICKER, 36))
            // API 10 and 11 check metadata below the application's API 24 execution minimum.
            assertNotNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 10))
            assertNull(family.unsupportedReason(LabComponent.CALENDAR_VIEW, 11))
        }
        listOf(LabComponent.DATE_PICKER, LabComponent.CALENDAR_VIEW, LabComponent.DATE_RANGE_PICKER)
            .forEach { component ->
                assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
            }
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_PICKER, 24))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_RANGE_PICKER, 24))
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.CALENDAR_VIEW, 36))
    }

    @Test
    fun popupMenuUsesFrameworkApi11AndBothPinnedLibraryFamilies() {
        // API 10 and 11 check metadata; the application itself runs from API 24.
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNotNull(family.unsupportedReason(LabComponent.POPUP_MENU, 10))
            assertNull(family.unsupportedReason(LabComponent.POPUP_MENU, 11))
        }
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.POPUP_MENU, 24))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.POPUP_MENU, 24))
    }

    @Test
    fun containersUseOnlyLibrariesThatSupplyTheirActualApi() {
        val shared = listOf(LabComponent.CARD, LabComponent.SURFACE)
        val material3Only = listOf(LabComponent.ELEVATED_CARD, LabComponent.OUTLINED_CARD)
        (shared + material3Only).forEach { component ->
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                assertNotNull(family.unsupportedReason(component, 36))
            }
            assertNull(DesignFamily.MATERIAL3.unsupportedReason(component, 24))
        }
        shared.forEach { component ->
            assertNull(DesignFamily.MATERIAL2.unsupportedReason(component, 24))
        }
        material3Only.forEach { component ->
            assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
        }
    }

    @Test
    fun timePickerDialogHasGenuineFrameworkAndMaterial3SuppliersOnly() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 24))
        }
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.TIME_PICKER_DIALOG, 24))
    }

    @Test
    fun datePickerDialogHasGenuineFrameworkAndMaterial3SuppliersOnly() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 24))
        }
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.DATE_PICKER_DIALOG, 24))
    }

    @Test
    fun frameworkAvailabilityUsesTheRunningOsRatherThanTheThemeOrigin() {
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SWITCH, 13))
        assertNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SWITCH, 14))
        assertNotNull(DesignFamily.HOLO.unsupportedReason(LabComponent.NUMBER_PICKER, 10))
        assertNull(DesignFamily.HOLO.unsupportedReason(LabComponent.NUMBER_PICKER, 11))
    }

    @Test
    fun frameworkOnlyWidgetsAreNotSubstitutedWithComposeRecreations() {
        listOf(
                LabComponent.TOGGLE_BUTTON,
                LabComponent.IMAGE_BUTTON,
                LabComponent.RATING,
                LabComponent.NUMBER_PICKER,
            )
            .forEach { component ->
                assertNull(DesignFamily.MATERIAL.unsupportedReason(component, 36))
                assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(component, 36))
                assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(component, 36))
            }
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.BUTTON, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.RANGE_SLIDER, 36))
    }

    @Test
    fun newerLibraryVariantsAreUnavailableInOlderFamilies() {
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.TONAL_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.OUTLINED_BUTTON, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.CHIP, 36))
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.CHIP, 36))
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.MULTI_SEGMENTED, 36))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.MULTI_SEGMENTED, 36))
        assertNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.SECURE_TEXT_FIELD, 36))
        assertNull(
            DesignFamily.MATERIAL3.unsupportedReason(LabComponent.OUTLINED_SECURE_TEXT_FIELD, 36)
        )
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.SPINNER, 36))
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SEARCH_VIEW, 10))
        assertNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.SEARCH_VIEW, 11))
    }
}
