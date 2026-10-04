package xyz.gaon.componentory.catalog

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.navigation.ComponentoryApp
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class PlannedCatalogTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @After
    fun restoreEnglish() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
    }

    @Test
    fun plannedCountsAndProviderVersionsDescribeSourcesWithoutRunnableRows() {
        mode("PLANNED")
        count(130)
        mapOf("PLATFORM" to 52, "MATERIAL2" to 22, "MATERIAL3" to 56).forEach { (family, size) ->
            provider(family)
            count(size)
            compose.onNodeWithTag("planned_provider_$family").assertIsSelected()
        }
        search("Snackbar")
        count(2)
        val identity = "MATERIAL3_androidx.compose.material3.Snackbar"
        showRow(identity)
        compose
            .onNodeWithTag("planned_$identity")
            .assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
        compose
            .onNodeWithTag("source_$identity", useUnmergedTree = true)
            .assertTextEquals("androidx.compose.material3.Snackbar")
        compose
            .onNodeWithTag("provider_$identity", useUnmergedTree = true)
            .assertTextEquals("Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}")
        compose
            .onNodeWithTag("status_$identity", useUnmergedTree = true)
            .assertTextEquals(compose.activity.getString(R.string.planned_status))
        compose.onNodeWithTag("detail_screen").assertDoesNotExist()
        provider("PLATFORM")
        count(0)
        compose.onNodeWithTag("planned_empty").assertIsDisplayed()
        search("ListPopupWindow")
        count(1)
        val framework = "PLATFORM_android.widget.ListPopupWindow"
        showRow(framework)
        compose
            .onNodeWithTag("provider_$framework", useUnmergedTree = true)
            .assertTextEquals(
                compose.activity.getString(R.string.planned_provider_framework) +
                    " · " +
                    compose.activity.getString(R.string.planned_api_introduced, 11)
            )
        provider("MATERIAL2")
        search("BottomAppBar")
        count(1)
        val material2 = "MATERIAL2_androidx.compose.material.BottomAppBar"
        showRow(material2)
        compose
            .onNodeWithTag("provider_$material2", useUnmergedTree = true)
            .assertTextEquals("Compose Material 2 · ${BuildConfig.MATERIAL2_VERSION}")
    }

    @Test
    fun missingSampleLinkPreservesQueryAndResetsOnlyThePlannedProvider() {
        mode("PLANNED")
        count(130)
        provider("MATERIAL2")
        mode("SAMPLES")
        compose.onNodeWithTag("list_category_SELECTION").performClick()
        search("Snackbar")
        compose.onNodeWithTag("search_empty").assertIsDisplayed()
        compose
            .onNodeWithTag("show_planned_matches")
            .assertTextEquals(compose.activity.getString(R.string.planned_view_matches, 4))
            .performClick()
        query("Snackbar")
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsSelected()
        compose.onNodeWithTag("planned_provider_ALL").assertIsSelected()
        count(4)
        mode("SAMPLES")
        query("Snackbar")
        compose.onNodeWithTag("list_category_SELECTION").assertIsSelected()
        compose.onNodeWithTag("clear_search").performClick()
        compose.onNodeWithTag("list_CHECKBOX").assertIsDisplayed()
        compose.onNodeWithTag("list_BUTTON").assertDoesNotExist()
    }

    @Test
    fun eachModeKeepsItsScrollAndProviderAcrossTabsAndRecreation() {
        compose.onNodeWithTag("component_list").performScrollToNode(hasTestTag("list_DIALOG"))
        compose.onNodeWithTag("list_DIALOG").assertIsDisplayed()
        mode("PLANNED")
        count(130)
        provider("MATERIAL3")
        val identity = "MATERIAL3_androidx.compose.material3.Snackbar"
        showRow(identity)
        mode("SAMPLES")
        compose.onNodeWithTag("list_DIALOG").assertIsDisplayed()
        mode("PLANNED")
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        count(56)
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        count(56)
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsSelected()
        compose.onNodeWithTag("planned_provider_MATERIAL3").assertIsSelected()
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        mode("SAMPLES")
        compose.onNodeWithTag("list_DIALOG").assertIsDisplayed()
    }

    @Test
    fun everyLanguageLocalizesPendingStatusAndCountsWhileKeepingApiNames() {
        mode("PLANNED")
        count(130)
        search("Snackbar")
        AppLanguage.entries
            .filter { it != AppLanguage.SYSTEM }
            .forEach { language ->
                compose.onNodeWithTag("nav_settings").performClick()
                compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
                compose.waitUntil(5_000) { LanguagePreferences.read(compose.activity) == language }
                compose.waitForIdle()
                compose.onNodeWithTag("nav_list").performClick()
                count(4)
                query("Snackbar")
                val identity = "MATERIAL3_androidx.compose.material3.Snackbar"
                showRow(identity)
                compose
                    .onNodeWithTag("source_$identity", useUnmergedTree = true)
                    .assertTextEquals("androidx.compose.material3.Snackbar")
                compose
                    .onNodeWithTag("status_$identity", useUnmergedTree = true)
                    .assertTextEquals(compose.activity.getString(R.string.planned_status))
                compose
                    .onNodeWithTag("catalog_mode_PLANNED")
                    .assertTextEquals(compose.activity.getString(R.string.catalog_mode_planned))
            }
    }

    @Test
    fun compactLayoutKeepsPlannedNavigationSearchAndProviderFiltersReachable() {
        compose.runOnUiThread {
            compose.activity.setContent { Box(Modifier.width(360.dp)) { ComponentoryApp() } }
        }
        mode("PLANNED")
        count(130)
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsDisplayed()
        search("Snackbar")
        provider("MATERIAL3")
        count(2)
        showRow("MATERIAL3_androidx.compose.material3.SnackbarHost")
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        count(2)
        query("Snackbar")
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsSelected()
    }

    private fun mode(name: String) {
        compose.onNodeWithTag("catalog_mode_$name").performClick()
    }

    private fun provider(name: String) {
        compose
            .onNodeWithTag("planned_providers")
            .performScrollToNode(hasTestTag("planned_provider_$name"))
        compose.onNodeWithTag("planned_provider_$name").performClick()
    }

    private fun search(text: String) {
        compose.onNodeWithTag("component_search").performTextReplacement(text)
        compose.onNodeWithTag("component_search").performImeAction()
    }

    private fun query(text: String) {
        compose
            .onNodeWithTag("component_search")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString(text),
                )
            )
    }

    private fun count(size: Int) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag("planned_count").fetchSemanticsNodes().isNotEmpty()
        }
        compose
            .onNodeWithTag("planned_count")
            .assertTextEquals(
                compose.activity.resources.getQuantityString(R.plurals.planned_count, size, size)
            )
    }

    private fun showRow(identity: String) {
        compose.onNodeWithTag("planned_list").performScrollToNode(hasTestTag("planned_$identity"))
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
    }
}
