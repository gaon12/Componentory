package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class RangeSliderTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openRangeComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("component_picker").performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement("Range slider")
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_RANGE_SLIDER"))
        compose.onNodeWithTag("component_RANGE_SLIDER").performClick()
    }

    @Test
    fun bothLibrariesMoveBothThumbsAndKeepDisabledRangesAfterRecreation() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily("LEFT", family)
            chooseFamily("RIGHT", family)
            expandDetails("LEFT")
            compose
                .onNodeWithTag("source_LEFT")
                .assertTextEquals(family.source(LabComponent.RANGE_SLIDER))
            sample().performScrollTo().performTouchInput {
                swipe(Offset(width * 0.2f, center.y), Offset(width * 0.35f, center.y))
            }
            var range = range()
            assertTrue("Start thumb did not move: $range", range.first > 20)
            assertTrue("End thumb moved with the start: $range", range.second == 80)
            sample().performTouchInput {
                swipe(Offset(width * 0.8f, center.y), Offset(width * 0.65f, center.y))
            }
            range = range()
            assertTrue("End thumb did not move: $range", range.second < 80)
            assertTrue("Thumb order was reversed: $range", range.first <= range.second)
            compose.onNodeWithTag("status_RIGHT").assertTextEquals("Range: 20–80 / 100")
            val expected = "Range: ${range.first}–${range.second} / 100"
            compose.activityRule.scenario.recreate()
            compose.onNodeWithTag("status_LEFT").assertTextEquals(expected)
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            sample().performScrollTo().performTouchInput {
                swipe(Offset(width * 0.35f, center.y), Offset(width * 0.1f, center.y))
                click(Offset(width * 0.9f, center.y))
            }
            compose.onNodeWithTag("status_LEFT").assertTextEquals(expected)
            compose.onNodeWithTag("enabled").performScrollTo().performClick()
            compose.onNodeWithTag("reset").performClick()
            compose.onNodeWithTag("status_LEFT").assertTextEquals("Range: 20–80 / 100")
            compose.onNodeWithTag("status_RIGHT").assertTextEquals("Range: 20–80 / 100")
        }
    }

    @Test
    fun platformFamiliesExplainTheMissingDedicatedRangeWidget() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach {
            chooseFamily("LEFT", it)
            compose.onNodeWithTag("unsupported_LEFT").assertExists()
            compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
            compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        }
    }

    private fun sample() = compose.onNodeWithTag("library_LEFT")

    private fun range(): Pair<Int, Int> {
        val text =
            compose
                .onNodeWithTag("status_LEFT")
                .fetchSemanticsNode()
                .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
                .single()
                .text
        val values = Regex("Range: (\\d+)–(\\d+) / 100").matchEntire(text)!!.groupValues
        return values[1].toInt() to values[2].toInt()
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
