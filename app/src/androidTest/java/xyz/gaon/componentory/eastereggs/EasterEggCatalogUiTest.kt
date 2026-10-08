package xyz.gaon.componentory.eastereggs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Rule
import org.junit.Test
import xyz.gaon.componentory.MainActivity

class EasterEggCatalogUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun versionSearchOpensTheMatchingDetailsAndPreservesItAcrossRecreation() {
        compose.onNodeWithTag("component_search").performTextReplacement("Android 8.1")
        compose.onNodeWithTag("egg_8_1").assertIsDisplayed()
        compose.onNodeWithTag("egg_8_0").assertDoesNotExist()
        compose.onNodeWithTag("egg_8_1").performClick()
        compose.onNodeWithTag("egg_details_8_1").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("egg_details_8_1").assertIsDisplayed()
    }
}
