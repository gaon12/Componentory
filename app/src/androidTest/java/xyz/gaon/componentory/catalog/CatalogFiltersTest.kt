package xyz.gaon.componentory.catalog

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class CatalogFiltersTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun listCombinesSearchAndCategoryAndRetainsBothAfterRecreation() {
        compose.onNodeWithTag("list_category_SELECTION").performClick().assertIsSelected()
        compose.onNodeWithTag("list_CHECKBOX").assertExists()
        compose.onNodeWithTag("list_BUTTON").assertDoesNotExist()
        compose.onNodeWithTag("component_search").performTextReplacement("Checkbox")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("list_category_SELECTION").assertIsSelected()
        compose.onNodeWithTag("component_search").assertTextEquals("Checkbox")
        compose.onNodeWithTag("list_CHECKBOX").assertExists()
        compose.onNodeWithTag("component_search").performTextReplacement("Button")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("list_BUTTON").assertDoesNotExist()
        compose.onNodeWithTag("component_search").performTextReplacement("no matching component")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("show_all_components").performClick()
        compose.onNodeWithTag("list_category_ALL").assertIsSelected()
        assertEquals(
            "",
            compose
                .onNodeWithTag("component_search")
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text,
        )
        compose.onNodeWithTag("component_list").performScrollToNode(hasTestTag("list_BUTTON"))
        compose.onNodeWithTag("list_BUTTON").assertExists()
    }

    @Test
    fun comparisonPickerFiltersWithoutChangingTheCurrentSampleUntilSelection() {
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("component_picker").performClick()
        compose.onNodeWithTag("picker_category_SELECTION").performClick().assertIsSelected()
        compose.onNodeWithTag("component_BUTTON").assertDoesNotExist()
        compose.onNodeWithTag("picker_search").performTextReplacement("Tri-state")
        compose.onNodeWithTag("component_TRI_STATE_CHECKBOX").assertExists()
        compose.onNodeWithTag("picker_close").performClick()
        compose.onNodeWithTag("component_picker").assertTextEquals("Button")
        compose.onNodeWithTag("component_picker").performClick()
        compose.onNodeWithTag("picker_category_SELECTION").performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement("Tri-state")
        compose.onNodeWithTag("component_TRI_STATE_CHECKBOX").performClick()
        compose.onNodeWithTag("component_picker").assertTextEquals("Tri-state checkbox")
        compose.onNodeWithTag("unsupported_LEFT").assertExists()
    }
}
