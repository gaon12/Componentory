package xyz.gaon.componentory.testing

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

fun ComposeContentTestRule.openSettingsPage(page: String) {
    onNodeWithTag("nav_settings").performClick()
    val category = "settings_category_$page"
    if (onAllNodes(hasTestTag(category)).fetchSemanticsNodes().isEmpty()) {
        onNodeWithTag("settings_back").performClick()
    }
    onNodeWithTag(category).performScrollTo().performClick()
}
