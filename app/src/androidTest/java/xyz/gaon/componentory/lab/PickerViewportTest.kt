package xyz.gaon.componentory.lab

import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class PickerViewportTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun useEnglishAndKeepTheScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH)
        }
        compose.waitForIdle()
    }

    @Test
    fun landscapeConfigurationKeepsANarrowClockFullyVisibleAndTouchable() {
        val state = SampleState(initialTime24Hour = false)
        compose.runOnUiThread {
            compose.activity.setContent {
                val landscape =
                    Configuration(LocalConfiguration.current).apply {
                        orientation = Configuration.ORIENTATION_LANDSCAPE
                        screenWidthDp = 1280
                        screenHeightDp = 800
                    }
                ComponentoryTheme {
                    CompositionLocalProvider(LocalConfiguration provides landscape) {
                        Box(Modifier.width(280.dp)) {
                            Material3InlineTimeSample(
                                LabComponent.TIME_PICKER,
                                "LEFT",
                                Modifier.testTag("picker"),
                                state,
                            )
                        }
                    }
                }
            }
        }
        val viewport = compose.onNodeWithTag("time_viewport_LEFT").fetchSemanticsNode().boundsInRoot
        val picker = compose.onNodeWithTag("picker").fetchSemanticsNode()
        assertTrue(
            "The library uses a vertical clock in the narrow panel",
            picker.size.height > picker.size.width,
        )
        compose.onNodeWithTag("time_viewport_LEFT_hint").assertDoesNotExist()
        (1..12).forEach { hour ->
            val label =
                compose.onNode(
                    hasClickAction() and
                        !SemanticsMatcher.expectValue(
                            SemanticsProperties.Role,
                            Role.RadioButton,
                        ) and
                        (hasContentDescription("$hour hours") or
                            hasContentDescription("$hour o'clock"))
                )
            val bounds = label.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue(
                "Hour $hour stays inside the panel",
                bounds.left >= viewport.left && bounds.right <= viewport.right,
            )
        }
        compose
            .onNode(
                hasClickAction() and
                    !SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton) and
                    (hasContentDescription("9 hours") or hasContentDescription("9 o'clock"))
            )
            .performTouchInput { click() }
        compose.runOnIdle { assertEquals(9, SampleTimes.parts(state.timeMinutes).hour) }
    }

    @Test
    fun overflowButtonsRevealTheOriginalCalendarLastColumnAndReturnToTheStart() {
        val state = SampleState()
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(
                        Modifier.width(280.dp)
                            .verticalScroll(rememberScrollState())
                            .testTag("test_page")
                    ) {
                        Material3InlineDateSample(
                            LabComponent.DATE_PICKER,
                            "LEFT",
                            Modifier.testTag("picker"),
                            state,
                        )
                    }
                }
            }
        }
        compose.onNodeWithTag("date_viewport_LEFT_hint").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("date_viewport_LEFT_start").assertIsNotEnabled()
        compose
            .onNodeWithTag("date_viewport_LEFT_end")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("date_viewport_LEFT").performScrollTo().assertIsDisplayed()
        // The original day Surface exposes a full date; its drawn day number clears semantics.
        val lastDay = compose.onNode(hasText("Saturday, January 20, 2024") and hasClickAction())
        val page = compose.onNodeWithTag("test_page")
        val day = lastDay.fetchSemanticsNode()
        val pageBounds = page.fetchSemanticsNode().boundsInRoot
        if (day.positionInRoot.y < pageBounds.top) {
            page.performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, day.positionInRoot.y - pageBounds.top)
            }
        }
        val viewport = compose.onNodeWithTag("date_viewport_LEFT").fetchSemanticsNode().boundsInRoot
        val bounds = lastDay.assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The last column can be fully revealed",
            bounds.left >= viewport.left && bounds.right <= viewport.right,
        )
        lastDay.performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(SampleDates.utcMillis(2024, 1, 20), state.inlineDateUtcMillis)
        }
        compose.onNodeWithTag("date_viewport_LEFT_end").assertIsNotEnabled()
        compose
            .onNodeWithTag("date_viewport_LEFT_start")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.waitForIdle()
        compose.onNodeWithTag("date_viewport_LEFT_start").assertIsNotEnabled()
        compose.onNodeWithTag("date_viewport_LEFT_end").assertIsEnabled()
    }
}
