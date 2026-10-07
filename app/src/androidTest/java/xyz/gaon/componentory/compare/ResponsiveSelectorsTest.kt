package xyz.gaon.componentory.compare

import android.content.res.Configuration
import android.os.LocaleList
import android.view.WindowManager
import android.view.inspector.WindowInspector
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.icons.IconCatalog
import xyz.gaon.componentory.icons.IconPicker
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class ResponsiveSelectorsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun componentResultsRemainTouchableAboveTheRealKeyboard() {
        val selected = mutableStateOf(LabComponent.BUTTON)
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Box(Modifier.fillMaxSize()) {
                        ComponentPicker(selected.value) { selected.value = it }
                    }
                }
            }
        }
        compose.onNodeWithTag("component_picker").performClick()
        compose.onNodeWithTag("picker_search").performTouchInput { click() }
        waitForKeyboard()
        compose.onNodeWithTag("picker_search").performTextReplacement("NumberPicker")
        assertTrue(
            compose
                .onNodeWithTag("component_picker_list")
                .fetchSemanticsNode()
                .boundsInRoot
                .height > 0
        )
        compose.onNodeWithTag("component_NUMBER_PICKER").assertIsDisplayed().performTouchInput {
            click()
        }
        compose.onNodeWithTag("component_picker_dialog").assertDoesNotExist()
        assertEquals(LabComponent.NUMBER_PICKER, selected.value)
    }

    @Test
    fun iconResultsRemainTouchableAboveTheRealKeyboard() {
        val selected = mutableStateOf(IconCatalog.defaultMaterialIcon)
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Box(Modifier.fillMaxSize()) {
                        IconPicker(false, selected.value, "TEST") { selected.value = it }
                    }
                }
            }
        }
        compose.onNodeWithTag("icon_picker_TEST").performClick()
        compose.waitUntil(10_000) {
            compose
                .onAllNodes(androidx.compose.ui.test.hasTestTag("icons_loading"))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        compose.onNodeWithTag("icon_search").performTouchInput { click() }
        waitForKeyboard()
        compose.onNodeWithTag("icon_search").performTextReplacement("_360")
        val id = "androidx.compose.material.icons.filled._360Kt"
        assertTrue(compose.onNodeWithTag("icon_grid").fetchSemanticsNode().boundsInRoot.height > 0)
        // Mirrored variants sort before this icon; scroll within the viewport while the IME stays
        // open.
        compose.onNodeWithTag("icon_grid").performScrollToNode(hasTestTag("icon_entry_$id"))
        waitForKeyboard()
        compose.onNodeWithTag("icon_entry_$id").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithTag("icon_dialog").assertDoesNotExist()
        assertEquals(id, selected.value.id)
    }

    @Test
    fun aLongLocalizedSelectionUsesItsOwnRowAndDoesNotTruncateAt320Dp() {
        listOf("en", "ko", "ja", "zh-CN", "zh-TW").forEach { tag ->
            compose.runOnUiThread {
                val configuration = Configuration(compose.activity.resources.configuration)
                configuration.setLocales(LocaleList(Locale.forLanguageTag(tag)))
                val localized = compose.activity.createConfigurationContext(configuration)
                compose.activity.setContent {
                    CompositionLocalProvider(
                        LocalContext provides localized,
                        LocalConfiguration provides configuration,
                    ) {
                        ComponentoryTheme {
                            Box(Modifier.width(320.dp)) {
                                CompareScreen(
                                    LabComponent.EXTENDED_FAB,
                                    {},
                                    DesignFamily.MATERIAL2,
                                    {},
                                    DesignFamily.MATERIAL3,
                                    {},
                                )
                            }
                        }
                    }
                }
            }
            compose.waitForIdle()
            val heading = compose.onNodeWithTag("compare_heading").fetchSemanticsNode().boundsInRoot
            val picker = compose.onNodeWithTag("component_picker")
            assertTrue(
                "The selector must be below the heading in $tag",
                picker.fetchSemanticsNode().boundsInRoot.top >= heading.bottom,
            )
            val layouts = mutableListOf<TextLayoutResult>()
            picker.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue(
                "The selected label must expose its text layout in $tag",
                layouts.isNotEmpty(),
            )
            layouts.forEach {
                assertFalse("The selected label must remain complete in $tag", it.hasVisualOverflow)
            }
        }
    }

    @Test
    fun catalogSearchKeepsItsMatchingComponentAboveTheRealKeyboard() {
        compose.onNodeWithTag("component_search").performTouchInput { click() }
        waitForKeyboard()
        compose.onNodeWithTag("component_search").performTextReplacement("Switch")
        compose.onNodeWithTag("list_SWITCH").assertIsDisplayed().performTouchInput { click() }
        compose.onNodeWithTag("detail_screen").assertIsDisplayed()
    }

    @Test
    fun historySearchKeepsItsMatchingPublicClassAboveTheRealKeyboard() {
        compose.onNodeWithTag("catalog_mode_HISTORY").performClick()
        compose.waitUntil(10_000) {
            compose
                .onAllNodes(androidx.compose.ui.test.hasTestTag("history_version"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("history_version").performClick()
        compose.onNodeWithTag("history_api_21").performScrollTo().performClick()
        compose.onNodeWithTag("component_search").performTouchInput { click() }
        waitForKeyboard()
        compose.onNodeWithTag("component_search").performTextReplacement("Toolbar")
        compose.onNodeWithTag("history_android.widget.Toolbar").assertIsDisplayed()
        compose.onNodeWithTag("component_search").performImeAction()
        compose.onNodeWithTag("history_version").assertIsDisplayed()
    }

    private fun waitForKeyboard() {
        compose.waitUntil(10_000) {
            var visible = false
            compose.runOnUiThread {
                visible =
                    WindowInspector.getGlobalWindowViews().any {
                        ViewCompat.getRootWindowInsets(it)
                            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                    }
            }
            visible
        }
        compose.waitForIdle()
    }
}
