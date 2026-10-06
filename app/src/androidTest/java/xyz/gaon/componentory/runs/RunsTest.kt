package xyz.gaon.componentory.runs

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class RunsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearSavedRuns() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        File(compose.activity.filesDir, RunStore.FILE_NAME).delete()
        compose.waitForIdle()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @After fun removeSavedRuns() = File(compose.activity.filesDir, RunStore.FILE_NAME).delete()

    @Test
    fun aSavedRunReopensBothPanelsWithTheirInputs() {
        pickComponent("CheckBox", "component_CHECKBOX")
        onView(withId(R.id.sample_left)).perform(click())
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Off")
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("run_saved").assertIsDisplayed()

        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithTag("runs_screen").assertExists()
        val opens = compose.onAllNodesWithText(openLabel())
        opens.fetchSemanticsNodes().single()
        opens[0].performClick()

        compose.onNodeWithTag("compare_screen").assertExists()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("On")
        compose.onNodeWithTag("status_RIGHT").assertTextEquals("Off")
        compose.onNodeWithTag("enabled").performScrollTo().assertIsOff()
    }

    @Test
    fun aRunRowExpandsItsFullEnvironmentRecord() {
        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("nav_runs").performClick()
        val toggles = compose.onAllNodesWithText(environmentLabel())
        toggles.fetchSemanticsNodes().single()
        toggles[0].performClick()
        compose.onNodeWithText("api: ${android.os.Build.VERSION.SDK_INT}").assertIsDisplayed()
        compose
            .onNodeWithText("targetSdk: ${compose.activity.applicationInfo.targetSdkVersion}")
            .assertIsDisplayed()
    }

    @Test
    fun deletingARunRemovesItsRowAndExportWaitsForRecords() {
        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithTag("runs_empty").assertIsDisplayed()
        compose.onNodeWithTag("runs_export").assertIsNotEnabled()

        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithTag("runs_export").assertIsEnabled()
        compose.onAllNodesWithText(deleteLabel())[0].performClick()
        compose.onNodeWithTag("runs_empty").assertIsDisplayed()
        compose.onNodeWithTag("runs_export").assertIsNotEnabled()
    }

    private fun pickComponent(query: String, tag: String) {
        compose.onNodeWithTag("nav_compare").performClick()
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(query)
        compose.onNodeWithTag(tag).performClick()
        compose.onNodeWithTag("compare_screen").assertExists()
    }

    private fun openLabel() = compose.activity.getString(R.string.run_open)

    private fun deleteLabel() = compose.activity.getString(R.string.run_delete)

    private fun environmentLabel() = compose.activity.getString(R.string.run_environment)
}
