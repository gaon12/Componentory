package xyz.gaon.componentory.runs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.gaon.componentory.compare.SampleSetup
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.SampleState

class RunComparisonTest {
    private fun setup(component: LabComponent, family: DesignFamily, state: SampleState) =
        SampleSetup.capture(component, family, state, API)

    @Test
    fun aRecordRoundTripsBothPanelsAndTheEnabledFlag() {
        val left =
            setup(LabComponent.SLIDER, DesignFamily.MATERIAL2, SampleState(initialValue = 40))
        val right =
            setup(
                LabComponent.SLIDER,
                DesignFamily.HOLO,
                SampleState(initialValue = 70, initialText = "ignored"),
            )
        val record = comparisonRecord("r1", 100, left, right, false, mapOf("sdk" to "36"))
        assertEquals("SLIDER", record.component)
        assertEquals(mapOf("value" to "40"), record.leftInputs)
        assertEquals(mapOf("value" to "70"), record.rightInputs)

        val parsed = requireNotNull(RunRecord.fromJson(record.toJson()))
        assertFalse(parsed.enabled)
        val entry = requireNotNull(parsed.toComparisonEntry())
        assertFalse(entry.enabled)
        assertEquals(LabComponent.SLIDER, entry.left.component)
        val restoredLeft = requireNotNull(entry.left.copyTo(DesignFamily.MATERIAL2, API).state)
        assertEquals(40, restoredLeft.value)
        val restoredRight =
            requireNotNull(requireNotNull(entry.right).copyTo(DesignFamily.HOLO, API).state)
        assertEquals(70, restoredRight.value)
        assertNotSame(restoredLeft, restoredRight)
    }

    @Test
    fun storedInputsComeBackWithTheirOriginalTypes() {
        val left =
            setup(
                LabComponent.TIME_PICKER_DIALOG,
                DesignFamily.MATERIAL3,
                SampleState(initialTimeMinutes = 65, initialTime24Hour = false),
            )
        val right = setup(LabComponent.TIME_PICKER_DIALOG, DesignFamily.HOLO, SampleState())
        val record = comparisonRecord("r2", 200, left, right, true, emptyMap())
        assertEquals(mapOf("time" to "65", "time24Hour" to "false"), record.leftInputs)

        val entry =
            requireNotNull(requireNotNull(RunRecord.fromJson(record.toJson())).toComparisonEntry())
        val restored = requireNotNull(entry.left.copyTo(DesignFamily.CLASSIC, API).state)
        assertEquals(65, restored.timeMinutes)
        assertFalse(restored.time24Hour)
    }

    @Test
    fun mixedComponentsCannotShareOneRecord() {
        val left = setup(LabComponent.SLIDER, DesignFamily.MATERIAL2, SampleState())
        val right = setup(LabComponent.CHECKBOX, DesignFamily.MATERIAL3, SampleState())
        try {
            comparisonRecord("r3", 1, left, right, true, emptyMap())
        } catch (expected: IllegalArgumentException) {
            return
        }
        throw AssertionError("A run record must compare one component")
    }

    @Test
    fun reopeningRejectsNamesTheCatalogNoLongerKnows() {
        val left = setup(LabComponent.SLIDER, DesignFamily.MATERIAL2, SampleState())
        val right = setup(LabComponent.SLIDER, DesignFamily.MATERIAL3, SampleState())
        val record = comparisonRecord("r4", 1, left, right, true, emptyMap())
        assertNull(record.copy(component = "GONE").toComparisonEntry())
        assertNull(record.copy(leftFamily = "GONE").toComparisonEntry())
        assertNull(record.copy(rightFamily = "GONE").toComparisonEntry())
    }

    @Test
    fun recordsWithoutEnabledAreRejected() {
        val left = setup(LabComponent.SLIDER, DesignFamily.MATERIAL2, SampleState())
        val right = setup(LabComponent.SLIDER, DesignFamily.MATERIAL3, SampleState())
        val json = comparisonRecord("r5", 1, left, right, true, emptyMap()).toJson()
        assertNull(RunRecord.fromJson(json.replace("\"enabled\":\"true\"", "")))
    }

    private companion object {
        const val API = 36
    }
}
