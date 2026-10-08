package xyz.gaon.componentory.lab

import android.os.Build
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class ThemedClocksTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private val families
        get() =
            listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3, DesignFamily.EXPRESSIVE) +
                if (Build.VERSION.SDK_INT >= 31) listOf(DesignFamily.MATERIAL_YOU) else emptyList()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun modernClocksRenderWithAnExplicitDemoBadgeAndNoFrameworkDeprecationClaim() {
        families.forEach { family ->
            listOf(
                    LabComponent.TEXT_CLOCK,
                    LabComponent.ANALOG_CLOCK,
                    LabComponent.DIGITAL_CLOCK,
                    LabComponent.CHRONOMETER,
                )
                .forEach { component ->
                    show(family, component, SampleState())
                    compose
                        .onNodeWithTag("rendering_LEFT")
                        .performScrollTo()
                        .assertTextEquals(
                            compose.activity.getString(R.string.rendering_themed_demo)
                        )
                    compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
                    compose.onNodeWithTag("clock_note_LEFT").assertDoesNotExist()
                    if (component == LabComponent.ANALOG_CLOCK) {
                        val dial =
                            compose
                                .onNodeWithTag("clock_dial_LEFT")
                                .performScrollTo()
                                .assertIsDisplayed()
                                .fetchSemanticsNode()
                                .boundsInRoot
                        assertTrue(dial.width > 0)
                        assertEquals(dial.width, dial.height, 1f)
                    } else
                        compose
                            .onNodeWithTag("clock_text_LEFT")
                            .performScrollTo()
                            .assertIsDisplayed()
                }
        }
    }

    @Test
    fun narrowLargeFontClocksFitWithoutVisualOverflowAndContinueTicking() {
        families.forEach { family ->
            val state = SampleState(initialTime24Hour = false)
            compose.runOnUiThread {
                compose.activity.setContent {
                    ComponentoryTheme {
                        val density = LocalDensity.current.density
                        CompositionLocalProvider(LocalDensity provides Density(density, 1.8f)) {
                            Column(
                                Modifier.windowInsetsPadding(WindowInsets.safeDrawing).width(150.dp)
                            ) {
                                ThemedClockSample(
                                    family,
                                    LabComponent.TEXT_CLOCK,
                                    "LEFT",
                                    true,
                                    state,
                                )
                            }
                        }
                    }
                }
            }
            val results = mutableListOf<TextLayoutResult>()
            compose.onNodeWithTag("clock_text_LEFT").assertIsDisplayed().performSemanticsAction(
                SemanticsActions.GetTextLayoutResult
            ) {
                it(results)
            }
            assertTrue(results.isNotEmpty())
            assertFalse(results.single().hasVisualOverflow)
            val before = clockText()
            SystemClock.sleep(1100)
            compose.waitUntil(2500) { clockText() != before }
        }
    }

    @Test
    fun timerButtonsStartFreezeResumeResetAndRespectDisabledInput() {
        families.forEach { family ->
            val state = SampleState()
            show(family, LabComponent.CHRONOMETER, state)
            val buttonText = mutableListOf<TextLayoutResult>()
            compose
                .onNodeWithTag("chronometer_start_LEFT")
                .performScrollTo()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(buttonText) }
            assertTrue(buttonText.single().layoutInput.style.fontSize.value >= 16f)
            tap("chronometer_start_LEFT")
            val initialText = clockText()
            SystemClock.sleep(1200)
            compose.waitUntil(2500) { clockText() != initialText }
            tap("chronometer_stop_LEFT")
            val stopped =
                compose.runOnIdle {
                    assertEquals(0, state.value)
                    assertTrue(state.chronometerBaseMillis >= 1000)
                    state.chronometerBaseMillis
                }
            val frozenText = clockText()
            SystemClock.sleep(1100)
            assertEquals(frozenText, clockText())
            tap("chronometer_start_LEFT")
            compose.runOnIdle {
                assertEquals(1, state.value)
                assertTrue(System.currentTimeMillis() - state.chronometerBaseMillis >= stopped)
            }
            tap("chronometer_reset_LEFT")
            compose.runOnIdle { assertEquals(1, state.value) }
            tap("chronometer_stop_LEFT")
            tap("chronometer_reset_LEFT")
            compose.runOnIdle { assertEquals(0L, state.chronometerBaseMillis) }
            show(family, LabComponent.CHRONOMETER, state, enabled = false)
            listOf("start", "stop", "reset").forEach { action ->
                compose
                    .onNodeWithTag("chronometer_${action}_LEFT")
                    .performScrollTo()
                    .assertIsNotEnabled()
            }
        }
    }

    private fun clockText() =
        compose
            .onNodeWithTag("clock_text_LEFT")
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .single()
            .text

    private fun tap(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()

    private fun show(
        family: DesignFamily,
        component: LabComponent,
        state: SampleState,
        enabled: Boolean = true,
    ) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(
                        Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                            .width(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        key(family, component, state) {
                            SamplePanel("LEFT", family, {}, component, enabled, 0, state)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }
}
