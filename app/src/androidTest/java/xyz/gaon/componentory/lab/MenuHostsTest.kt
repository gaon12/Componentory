package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.widget.ActionMenuView
import android.widget.Toolbar
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
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class MenuHostsTest {
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
    fun nativeMenuHostsRenderTheRealWidgets() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.TOOLBAR, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.TOOLBAR, family)
            compose.runOnIdle {
                val toolbar = view("LEFT") as Toolbar
                assertEquals(
                    compose.activity.getString(R.string.toolbar_title),
                    toolbar.title.toString(),
                )
                assertEquals(
                    compose.activity.getString(R.string.toolbar_subtitle),
                    toolbar.subtitle.toString(),
                )
                assertNotNull(toolbar.navigationIcon)
                assertEquals(2, toolbar.menu.size())
            }
            status("LEFT", "No selection")

            configure(LabComponent.ACTION_MENU_VIEW, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.ACTION_MENU_VIEW, family)
            compose.runOnIdle {
                val menuView = view("LEFT") as ActionMenuView
                assertEquals(2, menuView.menu.size())
                assertTrue(menuView.menu.findItem(1).isEnabled)
            }
            status("LEFT", "No selection")
        }
    }

    @Test
    fun menuActionClicksCopyAcrossPanelsAndSurviveRecreation() {
        configure(LabComponent.TOOLBAR, DesignFamily.CLASSIC, DesignFamily.HOLO)
        invokeMenuItem("LEFT", 2)
        status("LEFT", "Selected: Option B")
        compose.runOnIdle {
            val toolbar = view("LEFT") as Toolbar
            // The framework renders the navigation icon as an ImageButton child
            // carrying the navigation content description.
            val nav = navigationButton(toolbar)
            assertNotNull(nav)
            nav!!.performClick()
        }
        status("LEFT", "Selected: Navigation")
        invokeMenuItem("LEFT", 1)
        status("LEFT", "Selected: Option A")
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Selected: Option A")
        status("LEFT", "Selected: Option A")

        recreateActivity()
        status("LEFT", "Selected: Option A")
        status("RIGHT", "Selected: Option A")

        configure(LabComponent.ACTION_MENU_VIEW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        invokeMenuItem("LEFT", 1)
        status("LEFT", "Selected: Option A")
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Selected: Option A")
    }

    @Test
    fun disabledItemsStopRespondingAndLibraryCellsExplainThemselves() {
        configure(LabComponent.ACTION_MENU_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        setEnabled(false)
        compose.runOnIdle {
            val menuView = view("LEFT") as ActionMenuView
            assertFalse(menuView.menu.findItem(1).isEnabled)
        }
        // The disabled item's action button cannot invoke the listener.
        tapActionView("LEFT", compose.activity.getString(R.string.option_a))
        status("LEFT", "No selection")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Action menu view.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun fiveLanguagesLocalizeMenuHostNotesAndStatuses() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.TOOLBAR, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", text.none)
            compose.onNodeWithTag("toolbar_note_LEFT").assertTextEquals(text.note)
            invokeMenuItem("LEFT", 1)
            status("LEFT", String.format(text.selected, text.optionA))
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

    private fun view(panel: String): View = compose.activity.findViewById(nativeId(panel))

    private fun navigationButton(toolbar: Toolbar): View? {
        val label = compose.activity.getString(R.string.toolbar_navigate)
        for (index in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(index)
            if (child.contentDescription?.toString() == label) return child
        }
        return null
    }

    private fun invokeMenuItem(panel: String, itemId: Int) {
        // The framework renders each show-as-action item as a real text button
        // inside the host's ActionMenuView, so tap the rendered view.
        tapActionView(
            panel,
            compose.activity.getString(if (itemId == 1) R.string.option_a else R.string.option_b),
        )
    }

    private fun tapActionView(panel: String, label: String) {
        onView(allOf(withText(label), isDescendantOfA(withId(nativeId(panel)))))
            .perform(nativeClick())
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
            val widget = view(panel)
            assertEquals(ContextThemeWrapper::class.java, widget.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.colorBackground, expected, true),
                widget.context.theme.resolveAttribute(android.R.attr.colorBackground, actual, true),
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

    private data class MenuNames(
        val language: AppLanguage,
        val none: String,
        val selected: String,
        val optionA: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            MenuNames(
                AppLanguage.ENGLISH,
                "No selection",
                "Selected: %1\$s",
                "Option A",
                "Action and navigation clicks feed the last action state. Copying carries only that reported action.",
            ),
            MenuNames(
                AppLanguage.KOREAN,
                "선택 없음",
                "선택: %1\$s",
                "옵션 A",
                "작업 및 내비게이션 클릭이 마지막 작업 상태를 갱신합니다. 복사는 보고된 작업만 옮깁니다.",
            ),
            MenuNames(
                AppLanguage.JAPANESE,
                "選択なし",
                "選択: %1\$s",
                "選択肢 A",
                "アクションとナビゲーションのクリックが最後の操作状態を更新します。コピーは報告された操作のみを移します。",
            ),
            MenuNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "未选择",
                "已选择：%1\$s",
                "选项 A",
                "操作和导航点击会更新最后操作状态。复制仅转移所报告的操作。",
            ),
            MenuNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "未選取",
                "已選取：%1\$s",
                "選項 A",
                "操作與導覽點擊會更新最後操作狀態。複製僅轉移所回報的操作。",
            ),
        )
}
