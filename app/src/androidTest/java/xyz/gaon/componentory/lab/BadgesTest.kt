package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotEnabled
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
class BadgesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun libraryDotsAndNumberBadgesUseDifferentSizesAndCorrectSources() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily("LEFT", family)
            chooseComponent(LabComponent.DOT_BADGE)
            val dot =
                compose.onNodeWithTag("library_LEFT").performScrollTo().fetchSemanticsNode().size
            expandDetails("LEFT")
            compose
                .onNodeWithTag("source_LEFT")
                .assertTextEquals(family.source(LabComponent.DOT_BADGE))
            compose.onNodeWithTag("badge_count_LEFT", useUnmergedTree = true).assertDoesNotExist()
            chooseComponent(LabComponent.BADGE)
            val number =
                compose.onNodeWithTag("library_LEFT").performScrollTo().fetchSemanticsNode().size
            assertTrue(number.width > dot.width && number.height > dot.height)
            expandDetails("LEFT")
            compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(LabComponent.BADGE))
            count("LEFT", "7")
        }
    }

    @Test
    fun countedSamplesRespectBoundsDisabledInputResetAndSavedPanelState() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily("LEFT", family)
            chooseFamily("RIGHT", family)
            listOf(LabComponent.BADGE, LabComponent.BADGED_BOX).forEach { component ->
                chooseComponent(component)
                compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
                count("LEFT", "8")
                count("RIGHT", "7")
                compose.onNodeWithTag("library_LEFT").performTouchInput { click() }
                count("LEFT", "8")
                compose.onNodeWithTag("enabled").performScrollTo().performClick()
                compose
                    .onNodeWithTag("decrease_LEFT")
                    .performScrollTo()
                    .assertIsNotEnabled()
                    .performTouchInput { click() }
                count("LEFT", "8")
                compose.onNodeWithTag("enabled").performScrollTo().performClick()
                repeat(10) {
                    compose.onNodeWithTag("increase_ten_LEFT").performScrollTo().performClick()
                }
                count("LEFT", "100")
                compose.activityRule.scenario.recreate()
                count("LEFT", "100")
                count("RIGHT", "7")
                compose.onNodeWithTag("reset").performScrollTo().performClick()
                count("LEFT", "7")
                repeat(8) {
                    compose.onNodeWithTag("decrease_LEFT").performScrollTo().performClick()
                }
                count("LEFT", "0")
                compose.onNodeWithTag("reset").performScrollTo().performClick()
            }
        }
    }

    @Test
    fun anchoredBadgesRenderChosenIconsAndPlatformFamiliesShowTheirMissingWidget() {
        chooseComponent(LabComponent.BADGED_BOX)
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily("LEFT", family)
            compose.onNodeWithTag("icon_picker_LEFT").performScrollTo().performClick()
            compose.onNodeWithTag("icon_search").performTextReplacement("favorite")
            compose.onNodeWithTag("icon_style_FILLED").performClick()
            val id = "androidx.compose.material.icons.filled.FavoriteKt"
            compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
            compose.onNodeWithTag("icon_entry_$id").performClick()
            compose
                .onNodeWithTag("badge_icon_LEFT", useUnmergedTree = true)
                .assertContentDescriptionEquals("Favorite")
            expandDetails("LEFT")
            compose
                .onNodeWithTag("source_LEFT")
                .assertTextEquals(family.source(LabComponent.BADGED_BOX))
            compose.activityRule.scenario.recreate()
            compose
                .onNodeWithTag("badge_icon_LEFT", useUnmergedTree = true)
                .assertContentDescriptionEquals("Favorite")
        }
        listOf(LabComponent.BADGE, LabComponent.DOT_BADGE, LabComponent.BADGED_BOX).forEach {
            component ->
            chooseComponent(component)
            listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family
                ->
                chooseFamily("LEFT", family)
                compose.onNodeWithTag("unsupported_LEFT").assertExists()
                compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
                compose.onNodeWithTag("increase_LEFT").assertDoesNotExist()
            }
        }
    }

    private fun count(panel: String, value: String) {
        compose.onNodeWithTag("badge_count_$panel", useUnmergedTree = true).assertTextEquals(value)
        compose.onNodeWithTag("status_$panel").assertTextEquals("Badge count: $value")
    }

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
