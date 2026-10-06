package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class DividersTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val dividers =
        listOf(
            LabComponent.HORIZONTAL_DIVIDER,
            LabComponent.VERTICAL_DIVIDER,
            LabComponent.LEGACY_DIVIDER,
        )

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun libraryDividersUseTheCorrectOrientationAndKeepTheirOwnSourceIdentity() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily(family)
            dividers.forEach { component ->
                chooseComponent(component)
                if (family.unsupportedReason(component, 36) != null) {
                    compose.onNodeWithTag("unsupported_LEFT").assertExists()
                    compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
                } else {
                    val sample = compose.onNodeWithTag("library_LEFT").performScrollTo()
                    val size = sample.fetchSemanticsNode().size
                    assertTrue(size.width > 0 && size.height > 0)
                    if (component == LabComponent.VERTICAL_DIVIDER)
                        assertTrue(size.height > size.width * 10)
                    else assertTrue(size.width > size.height * 10)
                    expandDetails("LEFT")
                    compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
                    val feedback = "Preview: ${component.label}"
                    sample.performTouchInput { click() }
                    compose.onNodeWithTag("status_LEFT").assertTextEquals(feedback)
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                    sample.performScrollTo().performTouchInput { click() }
                    compose.onNodeWithTag("status_LEFT").assertTextEquals(feedback)
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                }
            }
        }
    }

    @Test
    fun platformFamiliesDoNotSubstituteCustomViewsForDedicatedLibraryDividers() {
        dividers.forEach { component ->
            chooseComponent(component)
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                chooseFamily(family)
                compose.onNodeWithTag("unsupported_LEFT").assertExists()
                compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
            }
        }
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
