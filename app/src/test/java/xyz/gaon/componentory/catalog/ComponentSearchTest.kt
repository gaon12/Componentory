package xyz.gaon.componentory.catalog

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.gaon.componentory.lab.LabComponent

class ComponentSearchTest {
    @Test
    fun emptySearchShowsTheWholeCatalog() {
        assertEquals(LabComponent.entries, search("  "))
    }

    @Test
    fun searchAcceptsNamesClassesAndKoreanDescriptions() {
        assertEquals(listOf(LabComponent.SWITCH), search("  sWITCH  "))
        assertEquals(listOf(LabComponent.TEXT_FIELD), search("EditText"))
        assertEquals(listOf(LabComponent.RADIO), search("라디오"))
        assertEquals(emptyList<LabComponent>(), search("unknown component"))
        assertEquals(listOf(LabComponent.TONAL_BUTTON), search("FilledTonalButton"))
    }

    private fun search(query: String) = LabComponent.entries.filter { it.matchesSearch(query) }
}
