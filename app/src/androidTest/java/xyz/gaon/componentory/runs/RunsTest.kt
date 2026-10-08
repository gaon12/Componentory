package xyz.gaon.componentory.runs

import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class RunsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var originalHistory: ByteArray? = null
    private var historyCaptured = false

    @Before
    fun clearSavedRuns() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val file = File(compose.activity.filesDir, RunStore.FILE_NAME)
        originalHistory = if (file.exists()) file.readBytes() else null
        historyCaptured = true
        if (file.exists()) assertTrue(file.delete())
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @After
    fun restoreSavedRuns() {
        if (!historyCaptured) return
        val file = File(compose.activity.filesDir, RunStore.FILE_NAME)
        originalHistory?.let { file.writeBytes(it) } ?: file.delete()
    }

    @Test
    fun reselectingRunsClosesExpandedDetailsWithoutDeletingTheRecord() {
        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        awaitTag("run_saved")
        compose.onNodeWithTag("nav_runs").performClick()
        val toggle = compose.onAllNodesWithText(environmentLabel())[0]
        toggle.performClick()
        compose.onNodeWithText("api: ${android.os.Build.VERSION.SDK_INT}").assertExists()
        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithText("api: ${android.os.Build.VERSION.SDK_INT}").assertDoesNotExist()
        compose.onAllNodesWithText(openLabel()).fetchSemanticsNodes().single()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("api: ${android.os.Build.VERSION.SDK_INT}").assertDoesNotExist()
        compose.onAllNodesWithText(openLabel()).fetchSemanticsNodes().single()
    }

    @Test
    fun aSavedRunReopensBothPanelsWithTheirInputs() {
        pickComponent("CheckBox", "component_CHECKBOX")
        compose.onNodeWithTag("native_LEFT").performScrollTo().assertIsDisplayed()
        onView(withId(R.id.sample_left)).perform(click())
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals(compose.activity.getString(R.string.sample_state_checked))
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextEquals(compose.activity.getString(R.string.sample_state_unchecked))
        compose.onNodeWithTag("enabled").performScrollTo().performClick()
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        awaitTag("run_saved")
        compose.onNodeWithTag("run_saved").assertIsDisplayed()

        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithTag("runs_screen").assertExists()
        val opens = compose.onAllNodesWithText(openLabel())
        opens.fetchSemanticsNodes().single()
        opens[0].performClick()

        compose.onNodeWithTag("compare_screen").assertExists()
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals(compose.activity.getString(R.string.sample_state_checked))
        compose
            .onNodeWithTag("status_RIGHT")
            .assertTextEquals(compose.activity.getString(R.string.sample_state_unchecked))
        compose.onNodeWithTag("enabled").performScrollTo().assertIsOff()
    }

    @Test
    fun aRunRowExpandsItsFullEnvironmentRecord() {
        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        awaitTag("run_saved")
        compose.onNodeWithTag("nav_runs").performClick()
        val toggles = compose.onAllNodesWithText(environmentLabel())
        toggles.fetchSemanticsNodes().single()
        toggles[0].performClick()
        compose
            .onNodeWithText("api: ${android.os.Build.VERSION.SDK_INT}")
            .performScrollTo()
            .assertIsDisplayed()
        compose
            .onNodeWithText("targetSdk: ${compose.activity.applicationInfo.targetSdkVersion}")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun deletingARunRemovesItsRowAndExportWaitsForRecords() {
        compose.onNodeWithTag("nav_runs").performClick()
        awaitTag("runs_empty")
        compose.onNodeWithTag("runs_empty").assertIsDisplayed()
        compose.onNodeWithTag("runs_export").assertIsNotEnabled()

        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        awaitTag("run_saved")
        compose.onNodeWithTag("nav_runs").performClick()
        compose.onNodeWithTag("runs_export").assertIsEnabled()
        compose.onAllNodesWithText(deleteLabel())[0].performClick()
        awaitTag("runs_empty")
        compose.onNodeWithTag("runs_empty").assertIsDisplayed()
        compose.onNodeWithTag("runs_export").assertIsNotEnabled()
    }

    @Test
    fun savingContinuesWhenTheUserOpensTheRunsTabImmediately() {
        pickComponent("Button", "component_BUTTON")
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("nav_runs").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(openLabel()).fetchSemanticsNodes().size == 1
        }
        compose.onNodeWithTag("runs_export").assertIsEnabled()
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

    private fun awaitTag(tag: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
