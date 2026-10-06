package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
class ProgressIndicatorsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun libraryModesExposeProgressAndCounterBoundsWithoutSharingValues() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily("LEFT", family)
            chooseFamily("RIGHT", family)
            listOf(
                    LabComponent.PROGRESS,
                    LabComponent.CIRCULAR_PROGRESS,
                    LabComponent.INDETERMINATE_LINEAR_PROGRESS,
                    LabComponent.INDETERMINATE_CIRCULAR_PROGRESS,
                )
                .forEach { component ->
                    chooseComponent(component)
                    compose.onNodeWithTag("library_LEFT").performScrollTo()
                    expandDetails("LEFT")
                    compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
                    if (component.isIndeterminateProgress) {
                        assertEquals(ProgressBarRangeInfo.Indeterminate, range())
                        compose.onNodeWithTag("increase_LEFT").assertDoesNotExist()
                        compose.onNodeWithTag("library_LEFT").performTouchInput { click() }
                        compose
                            .onNodeWithTag("status_LEFT")
                            .assertTextEquals("Progress: indeterminate")
                    } else {
                        assertEquals(0.5f, range().current, 0.001f)
                        repeat(6) {
                            compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
                        }
                        assertEquals(1f, range().current, 0.001f)
                        compose.onNodeWithTag("status_LEFT").assertTextEquals("Value: 100 / 100")
                        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Value: 50 / 100")
                        compose.activityRule.scenario.recreate()
                        compose.onNodeWithTag("status_LEFT").assertTextEquals("Value: 100 / 100")
                        compose.onNodeWithTag("enabled").performScrollTo().performClick()
                        compose
                            .onNodeWithTag("decrease_LEFT")
                            .performScrollTo()
                            .assertIsNotEnabled()
                            .performTouchInput { click() }
                        assertEquals(1f, range().current, 0.001f)
                        compose.onNodeWithTag("enabled").performScrollTo().performClick()
                        repeat(11) {
                            compose.onNodeWithTag("decrease_LEFT").performScrollTo().performClick()
                        }
                        assertEquals(0f, range().current, 0.001f)
                        compose.onNodeWithTag("reset").performScrollTo().performClick()
                        assertEquals(0.5f, range().current, 0.001f)
                    }
                }
        }
    }

    @Test
    fun platformCircularStylesExplainWhyDeterminateProgressIsUnavailable() {
        chooseComponent(LabComponent.CIRCULAR_PROGRESS)
        chooseFamily("RIGHT", DesignFamily.MATERIAL2)
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily("LEFT", family)
            compose.onNodeWithTag("unsupported_LEFT").assertExists()
            compose
                .onNodeWithText(
                    "Platform circular ProgressBar styles support indeterminate progress only."
                )
                .assertExists()
            compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("increase_LEFT").assertDoesNotExist()
        }
    }

    private fun range() =
        compose
            .onNodeWithTag("library_LEFT")
            .fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performClick()
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
