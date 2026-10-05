package xyz.gaon.componentory.compare

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class CompareStateTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var width by mutableStateOf(800.dp)
    private var leftFamily by mutableStateOf(DesignFamily.CLASSIC)
    private var rightFamily by mutableStateOf(DesignFamily.HOLO)

    @Test
    fun equalProviderTransitionsCannotMoveStateBetweenPanels() {
        showComparison(LabComponent.PROGRESS, DesignFamily.CLASSIC, DesignFamily.HOLO)
        repeat(5) { compose.onNodeWithTag("decrease_LEFT").performScrollTo().performClick() }
        status("LEFT", "Value: 0 / 100")
        status("RIGHT", "Value: 50 / 100")
        chooseFamily("LEFT", DesignFamily.HOLO)
        repeat(5) { compose.onNodeWithTag("decrease_LEFT").performScrollTo().performClick() }
        status("LEFT", "Value: 0 / 100")
        status("RIGHT", "Value: 50 / 100")
        chooseFamily("LEFT", DesignFamily.MATERIAL)
        status("LEFT", "Value: 50 / 100")
        status("RIGHT", "Value: 50 / 100")
        compose.onNodeWithTag("increase_RIGHT").performScrollTo().performClick()
        chooseFamily("LEFT", DesignFamily.HOLO)
        status("LEFT", "Value: 50 / 100")
        status("RIGHT", "Value: 60 / 100")
        resize(360.dp)
        repeat(2) { compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick() }
        resize(800.dp)
        status("LEFT", "Value: 70 / 100")
        status("RIGHT", "Value: 60 / 100")
    }

    @Test
    fun nativeCountsRemainIndependentAcrossBothLayoutsAndStillReset() {
        showComparison(LabComponent.BUTTON, DesignFamily.CLASSIC, DesignFamily.MATERIAL)
        touchNative("LEFT")
        repeat(2) { touchNative("RIGHT") }
        counts(1, 2)
        resize(360.dp)
        counts(1, 2)
        touchNative("LEFT")
        touchNative("RIGHT")
        counts(2, 3)
        resize(800.dp)
        counts(2, 3)
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        resize(360.dp)
        touchNative("LEFT")
        counts(2, 3)
        compose.onNodeWithTag("reset").performScrollTo().performClick()
        counts(0, 0)
        resize(800.dp)
        counts(0, 0)
    }

    @Test
    fun libraryTextKeepsChangesMadeInEitherLayout() {
        showComparison(LabComponent.TEXT_FIELD, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        replaceText("LEFT", "left draft")
        replaceText("RIGHT", "right draft")
        resize(360.dp)
        text("LEFT", "left draft")
        text("RIGHT", "right draft")
        replaceText("LEFT", "edited in compact")
        resize(800.dp)
        text("LEFT", "edited in compact")
        text("RIGHT", "right draft")
        replaceText("RIGHT", "edited in wide")
        resize(360.dp)
        text("LEFT", "edited in compact")
        text("RIGHT", "edited in wide")
    }

    @Test
    fun bothLibraryRangeThumbsKeepTheirValuesWhenPanelsMove() {
        showComparison(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        drag("LEFT", 0.2f, 0.35f)
        drag("LEFT", 0.8f, 0.65f)
        drag("RIGHT", 0.2f, 0.3f)
        drag("RIGHT", 0.8f, 0.9f)
        val wideLeft = feedback("LEFT")
        val wideRight = feedback("RIGHT")
        assertNotEquals("Range: 20–80 / 100", wideLeft)
        assertNotEquals("Range: 20–80 / 100", wideRight)
        resize(360.dp)
        status("LEFT", wideLeft)
        status("RIGHT", wideRight)
        drag("LEFT", 0.65f, 0.9f)
        drag("RIGHT", 0.3f, 0.1f)
        val compactLeft = feedback("LEFT")
        val compactRight = feedback("RIGHT")
        assertNotEquals(wideLeft, compactLeft)
        assertNotEquals(wideRight, compactRight)
        resize(800.dp)
        status("LEFT", compactLeft)
        status("RIGHT", compactRight)
    }

    @Test
    fun chosenIconStylesAndBadgeCountsSurviveLayoutChanges() {
        showComparison(LabComponent.BADGED_BOX, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
        val favorite = "androidx.compose.material.icons.filled.FavoriteKt"
        val home = "androidx.compose.material.icons.outlined.HomeKt"
        chooseIcon("LEFT", "favorite", "FILLED", favorite)
        chooseIcon("RIGHT", "home", "OUTLINED", home)
        compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
        resize(360.dp)
        icon("LEFT", "Favorite", favorite)
        icon("RIGHT", "Home", home)
        status("LEFT", "Badge count: 8")
        status("RIGHT", "Badge count: 7")
        val rounded = "androidx.compose.material.icons.rounded.FavoriteKt"
        chooseIcon("LEFT", "favorite", "ROUNDED", rounded)
        resize(800.dp)
        icon("LEFT", "Favorite", rounded)
        icon("RIGHT", "Home", home)
        status("LEFT", "Badge count: 8")
        status("RIGHT", "Badge count: 7")
    }

    private fun showComparison(component: LabComponent, left: DesignFamily, right: DesignFamily) {
        compose.runOnUiThread {
            leftFamily = left
            rightFamily = right
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            compose.activity.setContent {
                ComponentoryTheme(dynamicColor = false) {
                    Box(Modifier.width(width)) {
                        CompareScreen(
                            component,
                            {},
                            leftFamily,
                            { leftFamily = it },
                            rightFamily,
                            { rightFamily = it },
                        )
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun resize(newWidth: Dp) {
        compose.runOnIdle { width = newWidth }
        val left = compose.onNodeWithTag("panel_LEFT").fetchSemanticsNode()
        val right = compose.onNodeWithTag("panel_RIGHT").fetchSemanticsNode()
        if (newWidth < 600.dp) {
            assertEquals(left.positionInRoot.x, right.positionInRoot.x, 1f)
            assertTrue(right.positionInRoot.y > left.positionInRoot.y + left.size.height)
        } else {
            assertEquals(left.positionInRoot.y, right.positionInRoot.y, 1f)
            assertTrue(right.positionInRoot.x > left.positionInRoot.x + left.size.width)
        }
    }

    private fun touchNative(panel: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo()
        onView(withId(if (panel == "LEFT") R.id.sample_left else R.id.sample_right))
            .perform(click())
    }

    private fun counts(left: Int, right: Int) {
        status("LEFT", "Clicks: $left")
        status("RIGHT", "Clicks: $right")
    }

    private fun replaceText(panel: String, text: String) {
        compose.onNodeWithTag("library_$panel").performScrollTo().performTextReplacement(text)
        onView(isRoot()).perform(closeSoftKeyboard())
    }

    private fun text(panel: String, text: String) {
        compose.onNodeWithTag("library_$panel").assertTextContains(text, substring = true)
        status(panel, "Text: $text")
    }

    private fun drag(panel: String, from: Float, to: Float) {
        compose.onNodeWithTag("library_$panel").performScrollTo().performTouchInput {
            swipe(Offset(width * from, center.y), Offset(width * to, center.y))
        }
    }

    private fun feedback(panel: String): String =
        compose
            .onNodeWithTag("status_$panel")
            .fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.Text]
            .single()
            .text

    private fun status(panel: String, text: String) {
        compose.onNodeWithTag("status_$panel").assertTextEquals(text)
    }

    private fun chooseIcon(panel: String, query: String, style: String, id: String) {
        compose.onNodeWithTag("icon_picker_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("icon_search").performTextReplacement(query)
        compose.onNodeWithTag("icon_style_$style").performClick()
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
        compose.onNodeWithTag("icon_entry_$id").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performClick()
    }

    private fun icon(panel: String, name: String, id: String) {
        compose
            .onNodeWithTag("badge_icon_$panel", useUnmergedTree = true)
            .assertContentDescriptionEquals(name)
        compose
            .onNodeWithTag("icon_source_$panel")
            .assertTextEquals("$id · icons ${BuildConfig.MATERIAL_ICONS_VERSION}")
    }
}
