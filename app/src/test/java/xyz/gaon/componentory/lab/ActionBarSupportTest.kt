package xyz.gaon.componentory.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.gaon.componentory.catalog.supportedFamilies

class ActionBarSupportTest {
    @Test
    fun classicThemeDoesNotClaimAFrameworkActionBar() {
        assertNotNull(DesignFamily.CLASSIC.unsupportedReason(LabComponent.ACTION_BAR, 37))
        assertEquals(
            listOf(DesignFamily.HOLO, DesignFamily.MATERIAL),
            supportedFamilies(LabComponent.ACTION_BAR, 37),
        )
    }

    @Test
    fun frameworkHostRequiresItsPublicApiAndDoesNotSubstituteComposeBars() {
        assertNotNull(DesignFamily.HOLO.unsupportedReason(LabComponent.ACTION_BAR, 10))
        assertNull(DesignFamily.HOLO.unsupportedReason(LabComponent.ACTION_BAR, 11))
        assertNotNull(DesignFamily.MATERIAL2.unsupportedReason(LabComponent.ACTION_BAR, 37))
        assertNotNull(DesignFamily.MATERIAL3.unsupportedReason(LabComponent.ACTION_BAR, 37))
    }
}
