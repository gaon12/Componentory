package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.Button
import android.widget.ListPopupWindow
import android.widget.PopupWindow
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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class PopupWindowsTest {
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
    fun nativePopupWindowsAnchorRealFloatingViews() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.POPUP_WINDOW, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.POPUP_WINDOW, family)
            status("LEFT", "Shown 0 times")
            openPopup("LEFT")
            compose.runOnIdle {
                val popup = button("LEFT").tag as PopupWindow
                assertEquals(PopupWindow::class.java, popup.javaClass)
                assertTrue(popup.isShowing)
                popup.dismiss()
            }
            status("LEFT", "Shown 1 times")

            configure(LabComponent.LIST_POPUP_WINDOW, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.LIST_POPUP_WINDOW, family)
            status("LEFT", "No selection")
            openPopup("LEFT")
            compose.runOnIdle {
                val popup = button("LEFT").tag as ListPopupWindow
                assertEquals(ListPopupWindow::class.java, popup.javaClass)
                assertTrue(popup.isShowing)
                assertEquals(4, popup.listView?.adapter?.count)
                popup.dismiss()
            }
            status("LEFT", "No selection")
        }
    }

    @Test
    fun popupReportsCopyAcrossPanelsAndSurviveRecreation() {
        configure(LabComponent.LIST_POPUP_WINDOW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        openPopup("LEFT")
        compose.runOnIdle {
            val popup = button("LEFT").tag as ListPopupWindow
            val list = requireNotNull(popup.listView)
            // An adapter item click feeds the selection and dismisses the popup.
            list.performItemClick(list.getChildAt(2), 2, list.adapter.getItemId(2))
        }
        status("LEFT", "Selected: Item 3")
        compose.runOnIdle { assertNull(button("LEFT").tag) }
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Selected: Item 3")
        status("LEFT", "Selected: Item 3")

        recreateActivity()
        status("LEFT", "Selected: Item 3")
        status("RIGHT", "Selected: Item 3")

        configure(LabComponent.POPUP_WINDOW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        openPopup("LEFT")
        compose.runOnIdle { (button("LEFT").tag as PopupWindow).dismiss() }
        openPopup("LEFT")
        compose.runOnIdle { (button("LEFT").tag as PopupWindow).dismiss() }
        status("LEFT", "Shown 2 times")
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Shown 2 times")
    }

    @Test
    fun disabledTriggerStaysClosedAndLibraryCellsExplainThemselves() {
        configure(LabComponent.POPUP_WINDOW, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        setEnabled(false)
        onView(withId(R.id.sample_left)).perform(nativeClick())
        compose.waitForIdle()
        compose.runOnIdle { assertNull(button("LEFT").tag) }
        status("LEFT", "Shown 0 times")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Popup window.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun fiveLanguagesLocalizePopupNotesAndStatuses() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.LIST_POPUP_WINDOW, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", text.none)
            compose.onNodeWithTag("popup_note_LEFT").assertTextEquals(text.note)
            openPopup("LEFT")
            compose.runOnIdle {
                val popup = button("LEFT").tag as ListPopupWindow
                val list = requireNotNull(popup.listView)
                list.performItemClick(list.getChildAt(0), 0, list.adapter.getItemId(0))
            }
            status("LEFT", String.format(text.selected, text.item(1)))
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

    private fun button(panel: String): Button = compose.activity.findViewById(nativeId(panel))

    private fun openPopup(panel: String) {
        compose.runOnIdle { button(panel).performClick() }
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
            val view = button(panel)
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

    private data class PopupNames(
        val language: AppLanguage,
        val none: String,
        val selected: String,
        val item: (Int) -> String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            PopupNames(
                AppLanguage.ENGLISH,
                "No selection",
                "Selected: %1\$s",
                { "Item $it" },
                "The button opens the real popup window anchored to it. Copying carries the popup's reported state.",
            ),
            PopupNames(
                AppLanguage.KOREAN,
                "선택 없음",
                "선택: %1\$s",
                { "항목 $it" },
                "버튼을 누르면 버튼에 고정된 실제 팝업 창이 열립니다. 복사는 팝업이 보고한 상태만 옮깁니다.",
            ),
            PopupNames(
                AppLanguage.JAPANESE,
                "選択なし",
                "選択: %1\$s",
                { "項目 $it" },
                "ボタンで実際のポップアップウィンドウをそのボタンにアンカーして開きます。コピーはポップアップが報告した状態のみを移します。",
            ),
            PopupNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "未选择",
                "已选择：%1\$s",
                { "项目 $it" },
                "按钮会打开锚定到它的真实弹出窗口。复制仅转移弹窗所报告的状态。",
            ),
            PopupNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "未選取",
                "已選取：%1\$s",
                { "項目 $it" },
                "按鈕會開啟錨定到它的真實彈出視窗。複製僅轉移彈出視窗所回報的狀態。",
            ),
        )
}
