package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.QuickContactBadge
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences
import xyz.gaon.componentory.testing.openSettingsPage

@RunWith(AndroidJUnit4::class)
class ContentSurfacesTest {
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
    fun nativeSurfacesRenderTheRealWidgets() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.WEB_VIEW, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.WEB_VIEW, family)
            compose.waitUntil(10_000) {
                compose.runOnIdle { (view("LEFT") as WebView).contentHeight > 0 }
            }
            status("LEFT", "Preview: Web view")

            configure(LabComponent.QUICK_CONTACT_BADGE, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.QUICK_CONTACT_BADGE, family)
            compose.runOnIdle { assertNotNull((view("LEFT") as QuickContactBadge).drawable) }
            status("LEFT", "Preview: Quick contact badge")
        }
    }

    @Test
    fun surfacesHaveNoCopyableInputsAndLibraryCellsExplainThemselves() {
        configure(LabComponent.WEB_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        setEnabled(false)
        compose.runOnIdle { assertFalse(view("LEFT").isEnabled) }
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Web view.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
        chooseFamily("RIGHT", DesignFamily.HOLO)
        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
    }

    @Test
    fun recreationKeepsTheSurfacesAndTheirNotes() {
        configure(LabComponent.QUICK_CONTACT_BADGE, DesignFamily.CLASSIC, DesignFamily.HOLO)
        status("LEFT", "Preview: Quick contact badge")
        compose.onNodeWithTag("content_surface_note_LEFT").assertExists()
        recreateActivity()
        compose.runOnIdle { assertNotNull((view("LEFT") as QuickContactBadge).drawable) }
        status("LEFT", "Preview: Quick contact badge")
    }

    @Test
    fun fiveLanguagesLocalizeSurfaceNotesAndStatuses() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.WEB_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", String.format(text.preview, text.label))
            compose.onNodeWithTag("content_surface_note_LEFT").assertTextEquals(text.note)
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

    private fun assertNativeIdentity(panel: String, component: LabComponent, family: DesignFamily) {
        expandDetails(panel)
        compose
            .onNodeWithTag("source_$panel")
            .performScrollTo()
            .assertTextEquals(component.platformSource!!)
        expandDetails(panel)
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
        compose.openSettingsPage("LANGUAGE")
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

    private data class SurfaceNames(
        val language: AppLanguage,
        val label: String,
        val preview: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            SurfaceNames(
                AppLanguage.ENGLISH,
                "Web view",
                "Preview: %1\$s",
                "The widget renders real HTML. Page scroll and navigation are never copied, and host Enabled does not reach page content.",
            ),
            SurfaceNames(
                AppLanguage.KOREAN,
                "웹 뷰",
                "미리보기: %1\$s",
                "위젯이 실제 HTML을 렌더링합니다. 페이지 스크롤과 내비게이션은 복사되지 않으며, 호스트의 Enabled는 페이지 콘텐츠에 적용되지 않습니다.",
            ),
            SurfaceNames(
                AppLanguage.JAPANESE,
                "ウェブビュー",
                "プレビュー: %1\$s",
                "ウィジェットは実際のHTMLをレンダリングします。ページのスクロールやナビゲーションはコピーされず、ホストのEnabledはページコンテンツに届きません。",
            ),
            SurfaceNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "网页视图",
                "预览：%1\$s",
                "该小部件渲染真实 HTML。页面滚动与导航不会被复制，宿主 Enabled 不影响页面内容。",
            ),
            SurfaceNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "網頁檢視",
                "預覽：%1\$s",
                "小工具渲染真實 HTML。頁面捲動與導覽不會被複製，宿主 Enabled 不影響頁面內容。",
            ),
        )

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
