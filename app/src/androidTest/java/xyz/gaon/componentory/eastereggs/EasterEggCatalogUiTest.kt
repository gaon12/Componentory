package xyz.gaon.componentory.eastereggs

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.pressBack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.MainActivity

class EasterEggCatalogUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.activityRule.scenario.onActivity {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun backReturnsToTheFilteredCatalogAndReselectionReturnsToItsDefault() {
        open("8.1")
        compose.onNodeWithTag("detail_back").assertIsDisplayed()
        compose.onNodeWithTag("detail_compare").assertDoesNotExist()
        compose.onNodeWithTag("egg_stage_com.android_o.egg.octo.Ocquarium").assertDoesNotExist()
        pressBack()
        compose.onNodeWithTag("egg_details_8_1").assertDoesNotExist()
        compose.onNodeWithTag("egg_8_1").assertIsDisplayed().performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("egg_details_8_1").assertDoesNotExist()
        compose
            .onNodeWithTag("component_search")
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(""))
            )
    }

    @Test
    fun anotherTabPreservesTheEggPageAndTheToolbarReturnsToItsCatalog() {
        open("12L")
        compose
            .onNodeWithTag("egg_stage_com.android_s.egg.widget.PaintChipsActivity")
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        compose.onNodeWithTag("egg_details_12L").assertIsDisplayed()
        compose.onNodeWithTag("detail_back").performClick()
        compose.onNodeWithTag("egg_12L").assertIsDisplayed()
    }

    private fun open(version: String) {
        compose.onNodeWithTag("component_search").performTextReplacement("Android $version")
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("egg_${version.replace('.', '_')}").performClick()
    }

    @Test
    fun versionSearchOpensTheMatchingDetailsAndPreservesItAcrossRecreation() {
        compose.onNodeWithTag("component_search").performTextReplacement("Android 8.1")
        compose.onNodeWithTag("egg_8_1").assertIsDisplayed()
        compose.onNodeWithTag("egg_8_0").assertDoesNotExist()
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("egg_8_1").performClick()
        compose.onNodeWithTag("egg_details_8_1").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("egg_details_8_1").assertIsDisplayed()
    }
}
