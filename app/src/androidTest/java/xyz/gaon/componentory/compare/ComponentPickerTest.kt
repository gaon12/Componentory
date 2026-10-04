package xyz.gaon.componentory.compare

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class ComponentPickerTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchFindsNativeClassesAndCancelPreservesTheSelection() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("component_picker").performClick()
        compose.onNodeWithTag("component_BUTTON").assertIsSelected()
        compose.onNodeWithTag("picker_search").performTextReplacement("NumberPicker")
        compose.onNodeWithTag("component_NUMBER_PICKER").assertIsDisplayed().performClick()
        compose.onNodeWithTag("component_picker").assertTextEquals("Number picker")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Number: 5 / 10")
        compose.onNodeWithTag("component_picker").performClick()
        assertEquals(
            "",
            compose
                .onNodeWithTag("picker_search")
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text,
        )
        compose.onNodeWithTag("picker_search").performTextReplacement("nothing matches")
        compose.onNodeWithTag("picker_empty").assertIsDisplayed()
        compose.onNodeWithTag("picker_close").performClick()
        compose.onNodeWithTag("component_picker").assertTextEquals("Number picker")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("component_picker").assertTextEquals("Number picker")
    }
}
