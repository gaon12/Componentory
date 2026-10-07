package xyz.gaon.componentory.lab

import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.IconCatalog
import xyz.gaon.componentory.icons.LocalSampleIcon
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@RunWith(AndroidJUnit4::class)
class ExpressiveDesignsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun realThemesAndSavedColorsMatchTheirSuppliers() {
        val families =
            listOf(DesignFamily.MATERIAL3, DesignFamily.EXPRESSIVE) +
                if (Build.VERSION.SDK_INT >= 31) listOf(DesignFamily.MATERIAL_YOU) else emptyList()
        families.forEach { family ->
            var actual: ColorScheme? = null
            var motion: MotionScheme? = null
            compose.runOnUiThread {
                compose.activity.setContent {
                    ComponentoryTheme {
                        Material3SampleTheme(family) {
                            val colors = MaterialTheme.colorScheme
                            val scheme = MaterialTheme.motionScheme
                            SideEffect {
                                actual = colors
                                motion = scheme
                            }
                            Text("Theme probe")
                        }
                    }
                }
            }
            compose.waitForIdle()
            val expected =
                when (family) {
                    DesignFamily.EXPRESSIVE -> expressiveLightColorScheme()
                    DesignFamily.MATERIAL_YOU ->
                        if (Build.VERSION.SDK_INT >= 31) dynamicLightColorScheme(compose.activity)
                        else error("Unavailable dynamic color")
                    else -> lightColorScheme()
                }
            assertEquals(expected.primary, requireNotNull(actual).primary)
            assertEquals(expected.onPrimaryContainer, requireNotNull(actual).onPrimaryContainer)
            assertEquals(expected.surface, requireNotNull(actual).surface)
            val expectedMotion =
                if (family == DesignFamily.EXPRESSIVE) MotionScheme.expressive()
                else MotionScheme.standard()
            assertEquals(expectedMotion.javaClass, requireNotNull(motion).javaClass)
            val snapshot = sampleThemeSnapshot(family, compose.activity, "left")
            assertEquals(
                expected.primary.toArgb().toUInt().toString(16).padStart(8, '0'),
                snapshot["leftPrimaryArgb"],
            )
        }
    }

    @Test
    fun narrowButtonGroupKeepsOverflowActionsReachable() {
        val state = SampleState()
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).width(140.dp)) {
                        CompositionLocalProvider(
                            LocalSampleIcon provides IconCatalog.defaultMaterialIcon
                        ) {
                            Material3SampleTheme(DesignFamily.EXPRESSIVE) {
                                ExpressiveSample(LabComponent.BUTTON_GROUP, "LEFT", true, state)
                            }
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("expressive_overflow_LEFT").assertIsDisplayed().performClick()
        compose
            .onNode(
                hasText(compose.activity.getString(R.string.option_b)) and hasAnyAncestor(isPopup())
            )
            .assertIsDisplayed()
            .performClick()
        compose.runOnIdle { assertEquals(1, state.value) }
    }

    @Test
    fun toggleAndSplitActionsUseRealCheckedAndDisabledStates() {
        val toggle = SampleState()
        show(LabComponent.TOGGLE_BUTTON, toggle)
        compose
            .onNodeWithTag("library_LEFT")
            .performScrollTo()
            .assertIsOff()
            .performClick()
            .assertIsOn()
        compose.runOnIdle { assertEquals(1, toggle.value) }
        show(LabComponent.TOGGLE_BUTTON, toggle, enabled = false)
        compose.onNodeWithTag("library_LEFT").performScrollTo().assertIsNotEnabled().assertIsOn()

        val split = SampleState()
        show(LabComponent.SPLIT_BUTTON, split)
        compose.onNodeWithTag("expressive_primary_LEFT").performScrollTo().performClick()
        compose
            .onNodeWithTag("expressive_secondary_LEFT")
            .performScrollTo()
            .assertIsOff()
            .performClick()
            .assertIsOn()
        compose.runOnIdle { assertEquals(1, split.value) }
    }

    @Test
    fun indicatorsAndBothToolbarsRenderAndRespondToInput() {
        listOf(
                LabComponent.LOADING_INDICATOR,
                LabComponent.LINEAR_WAVY_PROGRESS,
                LabComponent.CIRCULAR_WAVY_PROGRESS,
            )
            .forEach { component ->
                val state = SampleState(45)
                show(component, state)
                compose.onNodeWithTag("library_LEFT").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
                compose.runOnIdle { assertEquals(55, state.value) }
            }
        listOf(LabComponent.HORIZONTAL_FLOATING_TOOLBAR, LabComponent.VERTICAL_FLOATING_TOOLBAR)
            .forEach { component ->
                show(component, SampleState())
                compose.onNodeWithTag("library_LEFT").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("expressive_expand_LEFT").performScrollTo().performClick()
                compose
                    .onNodeWithText(compose.activity.getString(R.string.option_a))
                    .assertIsDisplayed()
            }
    }

    @Test
    fun floatingMenuOpensSelectsAndHonorsDisabledInput() {
        val state = SampleState()
        show(LabComponent.FAB_MENU, state)
        compose.onNodeWithTag("expressive_expand_LEFT").performScrollTo().performClick()
        compose
            .onNodeWithText(compose.activity.getString(R.string.option_a))
            .assertIsDisplayed()
            .performClick()
        compose.runOnIdle { assertEquals(1, state.value) }
        show(LabComponent.FAB_MENU, state, enabled = false)
        compose.onNodeWithTag("expressive_expand_LEFT").performScrollTo().assertIsNotEnabled()
        compose.runOnIdle { assertTrue(state.value == 1) }
    }

    private fun show(component: LabComponent, state: SampleState, enabled: Boolean = true) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(
                        Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                            .verticalScroll(rememberScrollState())
                    ) {
                        key(component, state) {
                            SamplePanel(
                                "LEFT",
                                DesignFamily.EXPRESSIVE,
                                {},
                                component,
                                enabled,
                                0,
                                state,
                            )
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }
}
