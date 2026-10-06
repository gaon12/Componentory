package xyz.gaon.componentory.runs

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.compare.CompareScreen
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class RunSaveAcknowledgementTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun onlyACompletedSaveShowsSuccessAndFailuresAllowRetry() {
        var completed: ((Boolean) -> Unit)? = null
        compose.activity.setContent {
            ComponentoryTheme {
                CompareScreen(
                    LabComponent.BUTTON,
                    {},
                    DesignFamily.MATERIAL2,
                    {},
                    DesignFamily.MATERIAL3,
                    {},
                    onSaveRun = { _, result -> completed = result },
                )
            }
        }
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("save_run").assertIsNotEnabled()
        compose.onNodeWithTag("run_saved").assertDoesNotExist()
        compose.runOnIdle { requireNotNull(completed)(false) }
        compose.onNodeWithTag("save_run").assertIsEnabled().performClick()
        compose.runOnIdle { requireNotNull(completed)(true) }
        compose.onNodeWithTag("run_saved").assertExists()
    }

    @Test
    fun resettingTheComparisonPreventsAnOlderAcknowledgementFromClaimingSuccess() {
        var completed: ((Boolean) -> Unit)? = null
        compose.activity.setContent {
            ComponentoryTheme {
                CompareScreen(
                    LabComponent.BUTTON,
                    {},
                    DesignFamily.MATERIAL2,
                    {},
                    DesignFamily.MATERIAL3,
                    {},
                    onSaveRun = { _, result -> completed = result },
                )
            }
        }
        compose.onNodeWithTag("save_run").performScrollTo().performClick()
        compose.onNodeWithTag("reset").performClick()
        compose.runOnIdle { requireNotNull(completed)(true) }
        compose.onNodeWithTag("run_saved").assertDoesNotExist()
        compose.onNodeWithTag("save_run").assertIsEnabled()
    }
}
