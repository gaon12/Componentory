package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class LibrarySelectionsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val chips =
        listOf(
            LabComponent.CHIP,
            LabComponent.ASSIST_CHIP,
            LabComponent.ELEVATED_ASSIST_CHIP,
            LabComponent.FILTER_CHIP,
            LabComponent.ELEVATED_FILTER_CHIP,
            LabComponent.INPUT_CHIP,
            LabComponent.SUGGESTION_CHIP,
            LabComponent.ELEVATED_SUGGESTION_CHIP,
        )

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun chipsUseTheirOwnLibrariesAndIgnoreDisabledTouches() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily(family)
            chips.forEach { component ->
                chooseComponent(component)
                if (family.unsupportedReason(component, 36) != null) {
                    compose.onNodeWithTag("unsupported_LEFT").assertExists()
                    compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
                } else {
                    compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
                    val selectable =
                        component in
                            listOf(
                                LabComponent.FILTER_CHIP,
                                LabComponent.ELEVATED_FILTER_CHIP,
                                LabComponent.INPUT_CHIP,
                            )
                    sample().performScrollTo().performClick()
                    status(if (selectable) "Selected" else "Clicks: 1")
                    if (selectable) sample().assertIsSelected()
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                    sample().performScrollTo().performTouchInput { click() }
                    status(if (selectable) "Selected" else "Clicks: 1")
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                    compose.onNodeWithTag("reset").performClick()
                    status(if (selectable) "Not selected" else "Clicks: 0")
                    compose.onNodeWithTag("unsupported_RIGHT").assertExists()
                }
            }
        }
    }

    @Test
    fun triStateCheckboxCyclesThroughAllThreeStatesInBothLibraries() {
        chooseComponent(LabComponent.TRI_STATE_CHECKBOX)
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily(family)
            sample().performClick()
            status("Checked")
            sample().performClick()
            status("Indeterminate")
            assertEquals(
                ToggleableState.Indeterminate,
                sample().fetchSemanticsNode().config[SemanticsProperties.ToggleableState],
            )
            compose.onNodeWithTag("enabled").performClick()
            sample().performTouchInput { click() }
            status("Indeterminate")
            compose.onNodeWithTag("enabled").performClick()
            sample().performClick()
            status("Unchecked")
        }
    }

    @Test
    fun segmentedRowsKeepSingleAndMultipleSelectionsIndependentAcrossRecreation() {
        chooseFamily(DesignFamily.MATERIAL3)
        compose.onNodeWithTag("family_RIGHT").performClick()
        compose.onNodeWithTag("family_RIGHT_MATERIAL3").performClick()
        chooseComponent(LabComponent.SINGLE_SEGMENTED)
        compose.onNodeWithTag("library_LEFT_0").performClick().assertIsSelected()
        compose.onNodeWithTag("library_LEFT_2").performClick().assertIsSelected()
        status("Selected: C")
        compose.onNodeWithTag("library_LEFT_0").assertIsNotSelected()
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("No selection")
        chooseComponent(LabComponent.MULTI_SEGMENTED)
        compose.onNodeWithTag("library_LEFT_0").performClick()
        compose.onNodeWithTag("library_LEFT_2").performClick()
        status("Selected: A, C")
        compose.onNodeWithTag("library_RIGHT_1").performClick()
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Selected: B")
        compose.activityRule.scenario.recreate()
        status("Selected: A, C")
        compose.onNodeWithTag("enabled").performClick()
        compose.onNodeWithTag("library_LEFT_1").performTouchInput { click() }
        status("Selected: A, C")
        compose.onNodeWithTag("enabled").performClick()
        compose.onNodeWithTag("library_LEFT_0").performClick()
        status("Selected: C")
        compose.onNodeWithTag("reset").performClick()
        status("Selected: none")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Selected: none")
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }

    private fun sample() = compose.onNodeWithTag("library_LEFT")

    private fun status(text: String) {
        compose.onNodeWithTag("status_LEFT").assertTextEquals(text)
    }
}
