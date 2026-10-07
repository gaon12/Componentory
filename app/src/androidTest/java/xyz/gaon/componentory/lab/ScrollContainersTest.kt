package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
class ScrollContainersTest {
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
    fun nativeScrollContainersClipOverflowAndScrollWithRealTouch() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.SCROLL_VIEW, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.SCROLL_VIEW, family)
            compose.runOnIdle {
                val scroll = scrollView("LEFT") as ScrollView
                assertTrue(scroll.isShown && scroll.height > 0)
                val lines = scroll.getChildAt(0) as ViewGroup
                assertEquals(24, lines.childCount)
                assertTrue(
                    "The themed line column must overflow the bounded scroll view",
                    lines.height > scroll.height,
                )
            }
            compose.onNodeWithTag("native_LEFT").performTouchInput { swipeUp() }
            compose.runOnIdle {
                assertTrue("Touch input must move the real ScrollView", scrollY("LEFT") > 0)
            }
            compose
                .onNodeWithTag("scroll_note_LEFT")
                .assertTextEquals(
                    "Scrolling is free-form touch input on the real container. " +
                        "Scroll position is ephemeral and is not part of the copied setup."
                )
        }
    }

    @Test
    fun horizontalScrollContainerMovesSideways() {
        configure(LabComponent.HORIZONTAL_SCROLL_VIEW, DesignFamily.MATERIAL, DesignFamily.HOLO)
        assertNativeIdentity("LEFT", LabComponent.HORIZONTAL_SCROLL_VIEW, DesignFamily.MATERIAL)
        compose.runOnIdle {
            val scroll = scrollView("LEFT") as HorizontalScrollView
            val lines = scroll.getChildAt(0) as ViewGroup
            assertEquals(24, lines.childCount)
            assertTrue(lines.width > scroll.width)
        }
        compose.onNodeWithTag("native_LEFT").performTouchInput { swipeLeft() }
        compose.runOnIdle {
            assertTrue(
                "Touch input must move the real HorizontalScrollView",
                (scrollView("LEFT") as HorizontalScrollView).scrollX > 0,
            )
        }
    }

    @Test
    fun scrollContainersReportNoInputsAndUnsupportedLibraryTargets() {
        configure(LabComponent.SCROLL_VIEW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Preview: Scroll view")

        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 3 library does not provide Scroll view.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun fiveLanguagesLocalizeScrollLabelsNotesAndLines() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.SCROLL_VIEW, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            compose
                .onNodeWithTag("status_LEFT")
                .assertTextEquals("${text.preview}: ${text.scrollLabel}")
            compose.onNodeWithTag("scroll_note_LEFT").assertTextEquals(text.note)
            compose.runOnIdle {
                val lines = (scrollView("LEFT") as ScrollView).getChildAt(0) as ViewGroup
                assertEquals(
                    String.format(text.line, 1),
                    (lines.getChildAt(0) as android.widget.TextView).text.toString(),
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
        resetSamples()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performScrollTo().performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performScrollTo().performClick()
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

    private fun scrollView(panel: String): View = compose.activity.findViewById(nativeId(panel))

    private fun scrollY(panel: String): Int = (scrollView(panel) as ScrollView).scrollY

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
        compose.runOnIdle {
            val view = scrollView(panel)
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
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        keepScreenOn()
    }

    private fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private data class ScrollNames(
        val language: AppLanguage,
        val preview: String,
        val scrollLabel: String,
        val note: String,
        val line: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            ScrollNames(
                AppLanguage.ENGLISH,
                "Preview",
                "Scroll view",
                "Scrolling is free-form touch input on the real container. Scroll position is ephemeral and is not part of the copied setup.",
                "Scrollable line %1\$d",
            ),
            ScrollNames(
                AppLanguage.KOREAN,
                "미리보기",
                "스크롤 뷰",
                "스크롤은 실제 컨테이너의 자유로운 터치 입력입니다. 스크롤 위치는 임시 상태로 복사 대상에 포함되지 않습니다.",
                "스크롤 줄 %1\$d",
            ),
            ScrollNames(
                AppLanguage.JAPANESE,
                "プレビュー",
                "スクロールビュー",
                "スクロールは実際のコンテナへの自由なタッチ入力です。スクロール位置は一時的な状態であり、コピー対象には含まれません。",
                "スクロール行 %1\$d",
            ),
            ScrollNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "预览",
                "滚动视图",
                "滚动是对真实容器的自由触摸输入。滚动位置是临时状态，不属于复制的设置。",
                "滚动行 %1\$d",
            ),
            ScrollNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "預覽",
                "捲動檢視",
                "捲動是對真實容器的自由觸控輸入。捲動位置是暫時狀態，不屬於複製的設定。",
                "捲動行 %1\$d",
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
