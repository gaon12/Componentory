package xyz.gaon.componentory.lab

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.gaon.componentory.catalog.ComponentInventory
import xyz.gaon.componentory.catalog.InventoryStatus
import xyz.gaon.componentory.compare.SampleSetup
import xyz.gaon.componentory.lab.recreation.ResourceToasts
import xyz.gaon.componentory.lab.recreation.SampleRendering
import xyz.gaon.componentory.lab.recreation.renderingSnapshot
import xyz.gaon.componentory.lab.recreation.sampleRendering

class ToastRecreationTest {
    @Test
    fun everyDesignUsesExplicitAospArtworkWithoutClaimingALibraryToastApi() {
        val inventory =
            File("../docs/component-inventory.csv").reader().use(ComponentInventory::parse)
        val row = inventory.single { "TOAST" in it.catalogIds }
        assertEquals(InventoryStatus.RECREATED, row.status)
        assertEquals("android.widget.Toast", row.source)
        assertTrue(row.notes.contains("no system Toast"))
        DesignFamily.entries.forEach { family ->
            assertNull(family.unsupportedReason(LabComponent.TOAST, 35))
            val resources = ResourceToasts.forFamily(family)
            assertTrue(family.source(LabComponent.TOAST).contains(resources.release))
            assertFalse(family.source(LabComponent.TOAST).startsWith("androidx.compose"))
            assertEquals(
                SampleRendering.RESOURCE_RECREATION,
                sampleRendering(family, LabComponent.TOAST),
            )
            val saved = renderingSnapshot(family, LabComponent.TOAST, "left")
            assertEquals(resources.commit, saved["leftResourceCommit"])
            assertEquals("MISSING", saved["leftOriginalCapture"])
            assertEquals("COMPONENTORY_POPUP", saved["leftInteractionEngine"])
        }
        assertEquals(
            ResourceToasts.forFamily(DesignFamily.MATERIAL3),
            ResourceToasts.forFamily(DesignFamily.MATERIAL_YOU),
        )
        assertNotNull(DesignFamily.MATERIAL_YOU.unsupportedReason(LabComponent.TOAST, 30))
        assertEquals(
            617,
            LabComponent.entries.sumOf { component ->
                DesignFamily.entries.count { it.unsupportedReason(component, 35) == null }
            },
        )
    }

    @Test
    fun toastCountsCopyAcrossEveryDesignWithoutPersistingPopupOrTimerState() {
        DesignFamily.entries.forEach { from ->
            val setup =
                SampleSetup.capture(LabComponent.TOAST, from, SampleState(initialValue = 3), 35)
            assertEquals(setOf("component", "family", "value"), setup.savedValues().keys)
            DesignFamily.entries.forEach { to ->
                assertEquals(3, requireNotNull(setup.copyTo(to, 35).state).value)
            }
        }
    }
}
