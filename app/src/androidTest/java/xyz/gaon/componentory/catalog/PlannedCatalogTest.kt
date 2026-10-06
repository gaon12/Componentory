package xyz.gaon.componentory.catalog

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
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
        count(8)
        mapOf("PLATFORM" to 2, "MATERIAL2" to 0, "MATERIAL3" to 6).forEach { (family, size) ->
            provider(family)
            count(size)
            compose.onNodeWithTag("planned_provider_$family").assertIsSelected()
        }
        search("ExpandedDockedSearchBar")
        count(1)
        val identity = "MATERIAL3_androidx.compose.material3.ExpandedDockedSearchBar"
        showRow(identity)
        compose
            .onNodeWithTag("planned_$identity")
            .assertHasNoClickAction()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
        compose
            .onNodeWithTag("source_$identity", useUnmergedTree = true)
            .assertTextEquals("androidx.compose.material3.ExpandedDockedSearchBar")
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
        search("InlineContentView")
        count(1)
        val framework = "PLATFORM_android.widget.inline.InlineContentView"
        showRow(framework)
        compose
            .onNodeWithTag("provider_$framework", useUnmergedTree = true)
            .assertTextEquals(
                compose.activity.getString(R.string.planned_provider_framework) +
                    " · " +
                    compose.activity.getString(R.string.planned_api_introduced, 30)
            )
        provider("MATERIAL3")
        search("VerticalDragHandle")
        count(1)
        val library = "MATERIAL3_androidx.compose.material3.VerticalDragHandle"
        showRow(library)
        compose
            .onNodeWithTag("provider_$library", useUnmergedTree = true)
            .assertTextEquals("Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}")
        provider("ALL")
        listOf("DatePicker", "CalendarView", "DateRangePicker", "CheckedTextView", "TopSearchBar")
            .forEach { source ->
                search(source)
                count(0)
                compose.onNodeWithTag("planned_empty").assertIsDisplayed()
            }
        mode("SAMPLES")
        search("DatePicker")
        listOf("DATE_PICKER", "DATE_PICKER_DIALOG").forEach { component ->
            compose
                .onNodeWithTag("component_list")
                .performScrollToNode(hasTestTag("list_$component"))
            compose.onNodeWithTag("list_$component").assertIsDisplayed().assertHasClickAction()
        }
        search("CalendarView")
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_CALENDAR_VIEW"))
        compose.onNodeWithTag("list_CALENDAR_VIEW").assertIsDisplayed().assertHasClickAction()
        search("DateRangePicker")
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_DATE_RANGE_PICKER"))
        compose.onNodeWithTag("list_DATE_RANGE_PICKER").assertIsDisplayed().assertHasClickAction()
        search("CheckedTextView")
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_CHECKED_TEXT_VIEW"))
        compose.onNodeWithTag("list_CHECKED_TEXT_VIEW").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun missingSampleLinkPreservesQueryAndResetsOnlyThePlannedProvider() {
        mode("PLANNED")
        count(8)
        provider("MATERIAL2")
        mode("SAMPLES")
        compose.onNodeWithTag("list_category_SELECTION").performClick()
        search("AppBar")
        compose.onNodeWithTag("search_empty").assertIsDisplayed()
        compose
            .onNodeWithTag("show_planned_matches")
            .assertTextEquals(compose.activity.getString(R.string.planned_view_matches, 2))
            .performClick()
        query("AppBar")
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsSelected()
        compose.onNodeWithTag("planned_provider_ALL").assertIsSelected()
        count(2)
        mode("SAMPLES")
        query("AppBar")
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
        count(8)
        provider("MATERIAL3")
        val identity = "MATERIAL3_androidx.compose.material3.ExpandedDockedSearchBar"
        showRow(identity)
        mode("SAMPLES")
        compose.onNodeWithTag("list_DIALOG").assertIsDisplayed()
        mode("PLANNED")
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        count(6)
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        count(6)
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsSelected()
        compose.onNodeWithTag("planned_provider_MATERIAL3").assertIsSelected()
        compose.onNodeWithTag("planned_$identity").assertIsDisplayed()
        mode("SAMPLES")
        compose.onNodeWithTag("list_DIALOG").assertIsDisplayed()
    }

    @Test
    fun everyLanguageLocalizesPendingStatusAndCountsWhileKeepingApiNames() {
        mode("PLANNED")
        count(8)
        search("AppBar")
        AppLanguage.entries
            .filter { it != AppLanguage.SYSTEM }
            .forEach { language ->
                compose.onNodeWithTag("nav_settings").performClick()
                compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
                compose.waitUntil(5_000) { LanguagePreferences.read(compose.activity) == language }
                compose.waitForIdle()
                compose.onNodeWithTag("nav_list").performClick()
                count(2)
                query("AppBar")
                val identity = "MATERIAL3_androidx.compose.material3.AppBarRow"
                showRow(identity)
                compose
                    .onNodeWithTag("source_$identity", useUnmergedTree = true)
                    .assertTextEquals("androidx.compose.material3.AppBarRow")
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
        count(8)
        compose.onNodeWithTag("catalog_mode_SAMPLES").assertIsDisplayed()
        compose.onNodeWithTag("catalog_mode_PLANNED").assertIsDisplayed()
        search("AppBar")
        provider("MATERIAL3")
        count(2)
        showRow("MATERIAL3_androidx.compose.material3.AppBarColumn")
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        count(2)
        query("AppBar")
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
