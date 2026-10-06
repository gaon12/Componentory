package xyz.gaon.componentory.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

class ProviderSummaryTest {
    @Test
    fun componentsAvailableEverywhereListAllProviders() {
        listOf(LabComponent.BUTTON, LabComponent.CHECKBOX, LabComponent.RADIO).forEach { component
            ->
            assertEquals(DesignFamily.entries.toList(), supportedFamilies(component, 35))
        }
    }

    @Test
    fun libraryOnlyComponentsNameOnlyTheirLibraries() {
        assertEquals(
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3),
            supportedFamilies(LabComponent.SCAFFOLD, 35),
        )
        assertEquals(
            listOf(DesignFamily.MATERIAL3),
            supportedFamilies(LabComponent.EXPANDED_DOCKED_SEARCH_BAR, 35),
        )
    }

    @Test
    fun platformOnlyComponentsNameOnlyPlatformFamilies() {
        assertEquals(
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL),
            supportedFamilies(LabComponent.EDGE_EFFECT, 35),
        )
        assertEquals(
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL),
            supportedFamilies(LabComponent.TAB_HOST, 35),
        )
    }

    @Test
    fun everyCatalogComponentRunsOnAtLeastOneProvider() {
        LabComponent.entries.forEach { component ->
            assertTrue(
                "$component has no provider on the minimum runtime.",
                supportedFamilies(component, 24).isNotEmpty(),
            )
        }
    }
}
