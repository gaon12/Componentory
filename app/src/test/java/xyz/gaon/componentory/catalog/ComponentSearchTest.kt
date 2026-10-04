package xyz.gaon.componentory.catalog

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.gaon.componentory.lab.LabComponent

class ComponentSearchTest {
    @Test
    fun containerSourcesFindTheirGenuineSamples() {
        assertEquals(listOf(LabComponent.CARD), search("androidx.compose.material.Card"))
        assertEquals(listOf(LabComponent.CARD), search("androidx.compose.material3.Card"))
        assertEquals(listOf(LabComponent.SURFACE), search("androidx.compose.material.Surface"))
        assertEquals(listOf(LabComponent.SURFACE), search("androidx.compose.material3.Surface"))
        assertEquals(listOf(LabComponent.ELEVATED_CARD), search("ElevatedCard"))
        assertEquals(listOf(LabComponent.OUTLINED_CARD), search("OutlinedCard"))
    }

    @Test
    fun timeDialogIsSearchableByItsInteractiveSupportingApis() {
        assertEquals(listOf(LabComponent.TIME_PICKER_DIALOG), search("android.widget.TimePicker"))
        assertEquals(
            listOf(LabComponent.TIME_PICKER_DIALOG),
            search("androidx.compose.material3.TimeInput"),
        )
    }

    @Test
    fun emptySearchShowsTheWholeCatalog() {
        assertEquals(LabComponent.entries, search("  "))
    }

    @Test
    fun searchAcceptsCanonicalNamesAndClasses() {
        assertEquals(listOf(LabComponent.SWITCH), search("  sWITCH  "))
        assertEquals(listOf(LabComponent.TEXT_FIELD), search("EditText"))
        assertEquals(listOf(LabComponent.RADIO), search("Radio"))
        assertEquals(emptyList<LabComponent>(), search("unknown component"))
        assertEquals(listOf(LabComponent.TONAL_BUTTON), search("FilledTonalButton"))
    }

    private fun search(query: String) = LabComponent.entries.filter { it.matchesSearch(query) }
}
