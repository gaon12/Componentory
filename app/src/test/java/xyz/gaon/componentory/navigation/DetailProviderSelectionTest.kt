package xyz.gaon.componentory.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

class DetailProviderSelectionTest {
    @Test
    fun keepsTheCurrentProviderWheneverItSupportsTheComponent() {
        DesignFamily.entries.forEach { current ->
            assertEquals(current, selectDetailProvider(LabComponent.BUTTON, current, null, 36))
        }
    }

    @Test
    fun aLibraryOnlyComponentStartsWithTheFirstSupportedLibrary() {
        assertEquals(
            DesignFamily.MATERIAL2,
            selectDetailProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC, null, 36),
        )
    }

    @Test
    fun aMaterial3OnlyComponentFallsBackToMaterial3() {
        assertEquals(
            DesignFamily.MATERIAL3,
            selectDetailProvider(LabComponent.TONAL_BUTTON, DesignFamily.MATERIAL2, null, 36),
        )
    }

    @Test
    fun aFrameworkOnlyComponentStartsWithTheFirstFrameworkTheme() {
        assertEquals(
            DesignFamily.CLASSIC,
            selectDetailProvider(LabComponent.RATING, DesignFamily.MATERIAL3, null, 36),
        )
    }

    @Test
    fun remembersDeliberateUnsupportedChoicesEvenWhenAnotherProviderWorks() {
        assertEquals(
            DesignFamily.CLASSIC,
            selectDetailProvider(
                LabComponent.TONAL_BUTTON,
                DesignFamily.MATERIAL3,
                DesignFamily.CLASSIC,
                36,
            ),
        )
        assertEquals(
            DesignFamily.CLASSIC,
            selectDetailProvider(
                LabComponent.SWITCH,
                DesignFamily.MATERIAL2,
                DesignFamily.CLASSIC,
                13,
            ),
        )
    }

    // API 10, 13, and 14 are metadata inputs. The app itself requires API 24 or later.
    @Test
    fun usesTheRunningApiWhenDecidingWhetherToKeepTheCurrentProvider() {
        assertEquals(
            DesignFamily.MATERIAL2,
            selectDetailProvider(LabComponent.SWITCH, DesignFamily.CLASSIC, null, 13),
        )
        assertEquals(
            DesignFamily.CLASSIC,
            selectDetailProvider(LabComponent.SWITCH, DesignFamily.CLASSIC, null, 14),
        )
    }

    @Test
    fun keepsTheCurrentChoiceWhenNoProviderSupportsTheRuntime() {
        DesignFamily.entries.forEach { family ->
            assertNotNull(family.unsupportedReason(LabComponent.NUMBER_PICKER, 10))
        }
        assertEquals(
            DesignFamily.MATERIAL3,
            selectDetailProvider(LabComponent.NUMBER_PICKER, DesignFamily.MATERIAL3, null, 10),
        )
    }
}
