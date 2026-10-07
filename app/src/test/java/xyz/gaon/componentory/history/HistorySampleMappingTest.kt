package xyz.gaon.componentory.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import xyz.gaon.componentory.lab.LabComponent

class HistorySampleMappingTest {
    @Test
    fun publicButtonMapsToItsActualPlatformSample() {
        assertEquals(LabComponent.BUTTON, entry("android.widget.Button").currentSample())
    }

    @Test
    fun abstractAdapterBaseHasNoInventedSample() {
        assertNull(entry("android.widget.AbsSpinner").currentSample())
    }

    @Test
    fun frameworkChildrenMapToTheirActualContainers() {
        assertEquals(LabComponent.TAB_HOST, entry("android.widget.TabWidget").currentSample())
        assertEquals(LabComponent.TABLE_LAYOUT, entry("android.widget.TableRow").currentSample())
    }

    @Test
    fun referenceLinkUsesTheOfficialPackagePath() {
        assertEquals(
            "https://developer.android.com/reference/android/widget/Button",
            androidReferenceUrl("android.widget.Button"),
        )
    }

    @Test
    fun nestedApiTypesKeepTheirDocumentedTypeName() {
        assertEquals(
            "https://developer.android.com/reference/android/view/inputmethod/InlineSuggestionsRequest.Builder",
            androidReferenceUrl("android.view.inputmethod.InlineSuggestionsRequest.Builder"),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun referenceLinkRejectsAnUnrelatedClass() {
        androidReferenceUrl("example.widget.Button")
    }

    private fun entry(name: String) = HistoricalComponent(19, name, null, "VIEW", false, false)
}
