package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.gaon.componentory.compare.SampleSetup

class ModernDesignsTest {
    @Test
    fun dynamicColorRequiresItsActualPlatformApi() {
        assertNotNull(DesignFamily.MATERIAL_YOU.unsupportedReason(LabComponent.BUTTON, 30))
        assertNull(DesignFamily.MATERIAL_YOU.unsupportedReason(LabComponent.BUTTON, 31))
        assertEquals(
            "androidx.compose.material3.Button",
            DesignFamily.MATERIAL_YOU.source(LabComponent.BUTTON),
        )
        assertNotNull(DesignFamily.MATERIAL_YOU.unsupportedReason(LabComponent.RATING, 37))
    }

    @Test
    fun experimentalApisWorkWithEveryMaterial3ThemeButNotMaterial2() {
        val controls = LabComponent.entries.filter { it.experimentalMaterial3 }
        assertEquals(8, controls.size)
        controls.forEach { component ->
            DesignFamily.entries.forEach { family ->
                assertEquals(family.isMaterial3, family.unsupportedReason(component, 37) == null)
            }
            assertEquals(
                "androidx.compose.material3.${component.material3Function}",
                DesignFamily.EXPRESSIVE.source(component),
            )
        }
        assertEquals(
            "androidx.compose.material3.ToggleButton",
            DesignFamily.EXPRESSIVE.source(LabComponent.TOGGLE_BUTTON),
        )
        assertNull(DesignFamily.EXPRESSIVE.unsupportedReason(LabComponent.TOGGLE_BUTTON, 24))
        assertNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.TOGGLE_BUTTON, 37))
        assertEquals(
            "androidx.compose.material3.ToggleButton",
            DesignFamily.MATERIAL3.source(LabComponent.TOGGLE_BUTTON),
        )
    }

    @Test
    fun expressiveProgressTransfersOnlyItsDisplayedValue() {
        listOf(
                LabComponent.LOADING_INDICATOR,
                LabComponent.LINEAR_WAVY_PROGRESS,
                LabComponent.CIRCULAR_WAVY_PROGRESS,
            )
            .forEach { component ->
                val original = SampleState(initialValue = 73, initialText = "unrelated draft")
                val setup = SampleSetup.capture(component, DesignFamily.EXPRESSIVE, original, 37)
                val copy = requireNotNull(setup.copyTo(DesignFamily.EXPRESSIVE, 37).state)
                assertEquals(73, copy.value)
                assertEquals("", copy.text)
            }
    }
}
