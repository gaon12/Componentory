package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.gaon.componentory.compare.SampleSetup
import xyz.gaon.componentory.lab.recreation.SampleRendering
import xyz.gaon.componentory.lab.recreation.renderingSnapshot
import xyz.gaon.componentory.lab.recreation.sampleRendering

class ThemedClocksTest {
    private val clocks =
        listOf(
            LabComponent.TEXT_CLOCK,
            LabComponent.ANALOG_CLOCK,
            LabComponent.DIGITAL_CLOCK,
            LabComponent.CHRONOMETER,
        )

    @Test
    fun modernClocksExposeTheirOwnSourceAndNeverClaimAnOfficialMaterialClockApi() {
        DesignFamily.entries.forEach { family ->
            clocks.forEach { component ->
                assertNull(family.unsupportedReason(component, 37))
                if (family.platform == null) {
                    assertEquals(
                        "xyz.gaon.componentory.lab.ThemedClockSample",
                        family.source(component),
                    )
                    assertNull(family.libraryFunction(component))
                    assertEquals(SampleRendering.THEMED_DEMO, sampleRendering(family, component))
                    val saved = renderingSnapshot(family, component, "left")
                    assertEquals("COMPONENTORY_COMPOSE", saved["leftInteractionEngine"])
                    assertEquals("NOT_APPLICABLE", saved["leftOriginalCapture"])
                    assertFalse(saved.containsKey("leftResourceCommit"))
                } else assertTrue(family.source(component).startsWith("android.widget."))
            }
        }
        clocks.forEach { component ->
            assertNotNull(DesignFamily.MATERIAL_YOU.unsupportedReason(component, 30))
        }
    }

    @Test
    fun frameworkAndThemeDemosTransferClockFormatsAndRunningTimersWithoutInventingInputs() {
        DesignFamily.entries.forEach { from ->
            val text = SampleState(initialTime24Hour = false)
            val format = SampleSetup.capture(LabComponent.TEXT_CLOCK, from, text, 37)
            val timer = SampleState(initialValue = 1, initialChronometerBaseMillis = 123456L)
            val running = SampleSetup.capture(LabComponent.CHRONOMETER, from, timer, 37)
            DesignFamily.entries.forEach { to ->
                assertFalse(requireNotNull(format.copyTo(to, 37).state).time24Hour)
                val copied = requireNotNull(running.copyTo(to, 37).state)
                assertEquals(1, copied.value)
                assertEquals(123456L, copied.chronometerBaseMillis)
            }
        }
        val analog =
            SampleSetup.capture(
                LabComponent.ANALOG_CLOCK,
                DesignFamily.MATERIAL3,
                SampleState(),
                37,
            )
        assertEquals(setOf("component", "family"), analog.savedValues().keys)
    }
}
