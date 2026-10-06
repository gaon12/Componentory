package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TextView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class TabHostTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun nativeTabHostSwitchesContentThroughTheRealStrip() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.TAB_HOST, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.TAB_HOST, family)
            compose.runOnIdle {
                val host = host("LEFT")
                assertEquals(3, host.tabWidget.childCount)
                assertEquals(0, host.currentTab)
                // The setup() call built the real framework TabWidget strip.
                assertEquals(TabWidget::class.java, host.tabWidget.javaClass)
                assertEquals(
                    compose.activity.getString(R.string.tab_indicator, 1),
                    host.tabWidget
                        .getChildTabViewAt(0)
                        .findViewById<TextView>(android.R.id.title)
                        .text
                        .toString(),
                )
            }
            status("LEFT", "Selected tab 1 of 3")
            selectTab("LEFT", 1)
            compose.runOnIdle {
                val host = host("LEFT")
                assertEquals(1, host.currentTab)
                assertEquals(
                    compose.activity.getString(R.string.tab_content, 2),
                    (host.currentView as TextView).text.toString(),
                )
            }
            status("LEFT", "Selected tab 2 of 3")
            setEnabled(false)
            compose.runOnIdle { assertFalse(host("LEFT").tabWidget.isEnabled) }
            selectTab("LEFT", 2)
            compose.runOnIdle { assertEquals(1, host("LEFT").currentTab) }
        }
    }

    @Test
    fun tabCopiesCarryTheIndexAndRecreationRestoresIt() {
        configure(LabComponent.TAB_HOST, DesignFamily.CLASSIC, DesignFamily.HOLO)
        selectTab("LEFT", 2)
        status("LEFT", "Selected tab 3 of 3")
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle { assertEquals(2, host("RIGHT").currentTab) }
        status("RIGHT", "Selected tab 3 of 3")
        compose.runOnIdle { assertEquals(2, host("LEFT").currentTab) }

        recreateActivity()
        compose.runOnIdle {
            assertEquals(2, host("LEFT").currentTab)
            assertEquals(2, host("RIGHT").currentTab)
        }

        compose
            .onNodeWithTag("clock_note_LEFT")
            .performScrollTo()
            .assertTextContains("Deprecated since API 30", substring = true)

        configure(LabComponent.TAB_HOST, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Tab host.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun tabPanelsKeepIndependentIndexes() {
        configure(LabComponent.TAB_HOST, DesignFamily.CLASSIC, DesignFamily.HOLO)
        selectTab("LEFT", 1)
        selectTab("RIGHT", 2)
        compose.runOnIdle {
            assertEquals(1, host("LEFT").currentTab)
            assertEquals(2, host("RIGHT").currentTab)
        }
        status("LEFT", "Selected tab 2 of 3")
        status("RIGHT", "Selected tab 3 of 3")
        resetSamples()
        compose.runOnIdle {
            assertEquals(0, host("LEFT").currentTab)
            assertEquals(0, host("RIGHT").currentTab)
        }
    }

    @Test
    fun fiveLanguagesLocalizeTabLabelsNotesAndIndicators() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.TAB_HOST, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", String.format(text.selected, 1, 3))
            compose.onNodeWithTag("tab_note_LEFT").assertTextEquals(text.note)
            compose.runOnIdle {
                assertEquals(
                    String.format(text.indicator, 1),
                    host("LEFT")
                        .tabWidget
                        .getChildTabViewAt(0)
                        .findViewById<TextView>(android.R.id.title)
                        .text
                        .toString(),
                )
            }
        }
        changeLanguage(AppLanguage.ENGLISH)
    }

    private fun configure(component: LabComponent, left: DesignFamily, right: DesignFamily) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        chooseFamily("LEFT", left)
        chooseFamily("RIGHT", right)
        setEnabled(true)
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    @Suppress("DEPRECATION")
    private fun host(panel: String): TabHost = compose.activity.findViewById(nativeId(panel))

    @Suppress("DEPRECATION")
    private fun selectTab(panel: String, index: Int) {
        compose.runOnIdle { host(panel).tabWidget.getChildTabViewAt(index).performClick() }
        compose.waitForIdle()
    }

    private fun assertNativeIdentity(panel: String, component: LabComponent, family: DesignFamily) {
        compose
            .onNodeWithTag("source_$panel")
            .performScrollTo()
            .assertTextEquals(component.platformSource!!)
        compose
            .onNodeWithTag("implementation_$panel")
            .performScrollTo()
            .assertTextContains(
                "android:${requireNotNull(family.platform).themeName}",
                substring = true,
            )
            .assertTextContains(
                compose.activity.getString(R.string.widget_api, component.minimumApi),
                substring = true,
            )
        compose.runOnIdle {
            val view = host(panel)
            assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.colorBackground, expected, true),
                view.context.theme.resolveAttribute(android.R.attr.colorBackground, actual, true),
            )
            assertEquals(expected.resourceId, actual.resourceId)
        }
    }

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun blockedCopy(direction: String, reason: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_$direction", useUnmergedTree = true)
            .assertTextEquals(reason)
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != enabled
        )
            control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("language_${language.name}").performScrollTo().performClick()
        compose.waitUntil(10_000) { LanguagePreferences.read(compose.activity) == language }
        compose.waitForIdle()
        recreateActivity()
    }

    private fun recreateActivity() {
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class TabNames(
        val language: AppLanguage,
        val indicator: String,
        val selected: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            TabNames(
                AppLanguage.ENGLISH,
                "Tab %1\$d",
                "Selected tab %1\$d of %2\$d",
                "Tab taps switch the original TabHost content. Copying carries the selected tab index.",
            ),
            TabNames(
                AppLanguage.KOREAN,
                "탭 %1\$d",
                "선택된 탭 %1\$d/%2\$d",
                "탭을 누르면 원본 TabHost 콘텐츠가 전환됩니다. 복사는 선택된 탭 인덱스를 옮깁니다.",
            ),
            TabNames(
                AppLanguage.JAPANESE,
                "タブ %1\$d",
                "選択中のタブ %1\$d/%2\$d",
                "タブタップは元のTabHostコンテンツを切り替えます。コピーは選択中のタブインデックスを移します。",
            ),
            TabNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "标签 %1\$d",
                "已选中标签 %1\$d/%2\$d",
                "标签点击切换原始 TabHost 内容。复制转移所选标签索引。",
            ),
            TabNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "分頁 %1\$d",
                "已選取分頁 %1\$d/%2\$d",
                "分頁點擊切換原始 TabHost 內容。複製會轉移所選分頁索引。",
            ),
        )
}
