package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.gaon.componentory.catalog.CatalogMode
import xyz.gaon.componentory.catalog.supportedFamilies

class InlineContentSupportTest {
    @Test
    fun nativeInlineHostRequiresApi30InEveryPlatformTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            assertNotNull(family.unsupportedReason(LabComponent.INLINE_CONTENT_VIEW, 29))
            assertNull(family.unsupportedReason(LabComponent.INLINE_CONTENT_VIEW, 30))
        }
        assertEquals(
            "android.widget.inline.InlineContentView",
            DesignFamily.CLASSIC.source(LabComponent.INLINE_CONTENT_VIEW),
        )
        assertEquals(3, supportedFamilies(LabComponent.INLINE_CONTENT_VIEW, 30).size)
        assertNotNull(
            DesignFamily.MATERIAL2.unsupportedReason(LabComponent.INLINE_CONTENT_VIEW, 37)
        )
        assertNotNull(
            DesignFamily.MATERIAL3.unsupportedReason(LabComponent.INLINE_CONTENT_VIEW, 37)
        )
    }

    @Test
    fun oldPlannedStateRestoresToComponentsWithoutExposingAnEmptyTab() {
        assertEquals(CatalogMode.SAMPLES, CatalogMode.PLANNED.current())
        assertEquals(CatalogMode.HISTORY, CatalogMode.HISTORY.current())
        assertEquals(listOf(CatalogMode.SAMPLES, CatalogMode.HISTORY), CatalogMode.visibleModes)
    }
}
