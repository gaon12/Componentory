package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class MissingLibrarySamplesTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun absentLibrarySamplesExplainTheImplementationGapRatherThanThemeCompatibility() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            listOf(
                    LabComponent.CALENDAR_VIEW,
                    LabComponent.SCROLL_VIEW,
                    LabComponent.WEB_VIEW,
                    LabComponent.RATING,
                    LabComponent.NUMBER_PICKER,
                )
                .forEach { component ->
                    show(family, component)
                    compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
                    compose
                        .onNode(
                            hasText(compose.activity.getString(R.string.unsupported)) and
                                hasAnyAncestor(hasTestTag("unsupported_LEFT"))
                        )
                        .assertIsDisplayed()
                    compose
                        .onNodeWithTag("missing_sample_LEFT")
                        .performScrollTo()
                        .assertIsDisplayed()
                        .assertTextContains(
                            compose.activity.getString(R.string.missing_library_sample_note),
                            substring = true,
                        )
                    compose.onNodeWithTag("rendering_LEFT").assertDoesNotExist()
                }
        }
    }

    @Test
    fun nativeThemeRequirementsAndImplementedClockDemosKeepTheirDistinctExplanation() {
        show(DesignFamily.CLASSIC, LabComponent.ACTION_BAR)
        compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("missing_sample_LEFT").assertDoesNotExist()
        show(DesignFamily.MATERIAL3, LabComponent.TEXT_CLOCK)
        compose.onNodeWithTag("clock_text_LEFT").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("missing_sample_LEFT").assertDoesNotExist()
    }

    private fun show(family: DesignFamily, component: LabComponent) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(
                        Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                            .verticalScroll(rememberScrollState())
                    ) {
                        key(family, component) {
                            SamplePanel("LEFT", family, {}, component, true, 0, SampleState())
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }
}
