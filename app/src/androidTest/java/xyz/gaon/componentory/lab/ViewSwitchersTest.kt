package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
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
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
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

@RunWith(AndroidJUnit4::class)
class ViewSwitchersTest {
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
    fun nativeViewSwitchersStepChildrenAndRespectEnabledState() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.VIEW_ANIMATOR, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.VIEW_ANIMATOR, family)
            compose.runOnIdle {
                val animator = switcher("LEFT") as ViewAnimator
                assertEquals(4, animator.childCount)
                assertEquals(0, animator.displayedChild)
            }
            status("LEFT", "Showing child 1 of 4")
            tapSwitcher("LEFT", 1)
            compose.runOnIdle { assertEquals(1, switcher("LEFT").displayedChild) }
            status("LEFT", "Showing child 2 of 4")
            tapSwitcher("LEFT", 0)
            compose.runOnIdle { assertEquals(0, switcher("LEFT").displayedChild) }
            // Previous wraps from the first child to the last.
            tapSwitcher("LEFT", 0)
            compose.runOnIdle { assertEquals(3, switcher("LEFT").displayedChild) }
            status("LEFT", "Showing child 4 of 4")
            setEnabled(false)
            compose.runOnIdle {
                assertFalse(switcherControl("LEFT", 0).isEnabled)
                assertFalse(switcherControl("LEFT", 1).isEnabled)
            }
        }
    }

    @Test
    fun eachSwitcherShowsItsOwnContentThroughTheRealWidget() {
        mapOf(
                LabComponent.VIEW_SWITCHER to (ViewSwitcher::class.java to 2),
                LabComponent.VIEW_FLIPPER to (ViewFlipper::class.java to 4),
            )
            .forEach { (component, metadata) ->
                val (type, pages) = metadata
                configure(component, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
                assertNativeIdentity("LEFT", component, DesignFamily.MATERIAL)
                compose.runOnIdle {
                    val animator = switcher("LEFT")
                    assertEquals(type, animator.javaClass)
                    assertEquals(pages, animator.childCount)
                    assertEquals(0, animator.displayedChild)
                    assertEquals(
                        compose.activity.getString(R.string.switcher_page, 1),
                        (animator.getChildAt(0) as TextView).text.toString(),
                    )
                }
                tapSwitcher("LEFT", 1)
                compose.runOnIdle { assertEquals(1, switcher("LEFT").displayedChild) }
                status("LEFT", "Showing child 2 of $pages")
            }

        configure(LabComponent.TEXT_SWITCHER, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        assertNativeIdentity("LEFT", LabComponent.TEXT_SWITCHER, DesignFamily.HOLO)
        compose.runOnIdle {
            val switcher = switcher("LEFT") as TextSwitcher
            assertEquals(2, switcher.childCount)
            assertEquals(
                compose.activity.getString(R.string.switcher_line, 1),
                (switcher.currentView as TextView).text.toString(),
            )
        }
        status("LEFT", "Showing child 1 of 4")
        tapSwitcher("LEFT", 1)
        compose.runOnIdle {
            assertEquals(
                "TextSwitcher.setText must move the next line into view",
                compose.activity.getString(R.string.switcher_line, 2),
                ((switcher("LEFT") as TextSwitcher).currentView as TextView).text.toString(),
            )
        }
        status("LEFT", "Showing child 2 of 4")

        configure(LabComponent.IMAGE_SWITCHER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        assertNativeIdentity("LEFT", LabComponent.IMAGE_SWITCHER, DesignFamily.CLASSIC)
        compose.runOnIdle {
            val switcher = switcher("LEFT") as ImageSwitcher
            assertEquals(2, switcher.childCount)
            assertNotNull((switcher.currentView as ImageView).drawable)
        }
        tapSwitcher("LEFT", 1)
        status("LEFT", "Showing child 2 of 2")
        compose.runOnIdle {
            assertNotNull(((switcher("LEFT") as ImageSwitcher).currentView as ImageView).drawable)
        }
    }

    @Test
    fun switcherCopiesCarryTheIndexAndRecreationRestoresIt() {
        configure(LabComponent.VIEW_FLIPPER, DesignFamily.CLASSIC, DesignFamily.HOLO)
        tapSwitcher("LEFT", 1)
        tapSwitcher("LEFT", 1)
        status("LEFT", "Showing child 3 of 4")
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle { assertEquals(2, switcher("RIGHT").displayedChild) }
        status("RIGHT", "Showing child 3 of 4")
        // The copy did not rewind the source panel.
        compose.runOnIdle { assertEquals(2, switcher("LEFT").displayedChild) }

        recreateActivity()
        compose.runOnIdle {
            assertEquals(2, switcher("LEFT").displayedChild)
            assertEquals(2, switcher("RIGHT").displayedChild)
        }
        status("LEFT", "Showing child 3 of 4")

        configure(LabComponent.VIEW_SWITCHER, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide View switcher.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun switcherPanelsKeepIndependentChildIndexes() {
        configure(LabComponent.VIEW_ANIMATOR, DesignFamily.CLASSIC, DesignFamily.HOLO)
        tapSwitcher("LEFT", 1)
        tapSwitcher("RIGHT", 1)
        tapSwitcher("RIGHT", 1)
        compose.runOnIdle {
            assertEquals(1, switcher("LEFT").displayedChild)
            assertEquals(3, switcher("RIGHT").displayedChild)
        }
        status("LEFT", "Showing child 2 of 4")
        status("RIGHT", "Showing child 4 of 4")
        resetSamples()
        compose.runOnIdle {
            assertEquals(0, switcher("LEFT").displayedChild)
            assertEquals(0, switcher("RIGHT").displayedChild)
        }
    }

    @Test
    fun fiveLanguagesLocalizeSwitcherLabelsNotesAndButtons() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.VIEW_SWITCHER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", String.format(text.childStatus, 1, 2))
            compose.onNodeWithTag("switcher_note_LEFT").assertTextEquals(text.note)
            compose.runOnIdle {
                assertEquals(text.previous, switcherControl("LEFT", 0).text.toString())
                assertEquals(text.next, switcherControl("LEFT", 1).text.toString())
                assertEquals(
                    String.format(text.page, 1),
                    (switcher("LEFT").getChildAt(0) as TextView).text.toString(),
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

    private fun switcher(panel: String): ViewAnimator =
        compose.activity.findViewById(nativeId(panel))

    private fun switcherControl(panel: String, index: Int): Button {
        val root = switcher(panel).parent as ViewGroup
        return (root.getChildAt(1) as ViewGroup).getChildAt(index) as Button
    }

    private fun tapSwitcher(panel: String, index: Int) {
        val action = listOf("prev", "next")[index]
        onView(
                allOf(
                    withTagValue(equalTo("switcher_${action}_$panel")),
                    isAssignableFrom(Button::class.java),
                )
            )
            .perform(nativeClick())
        compose.waitForIdle()
    }

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
            val view = switcher(panel) as View
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

    private data class SwitcherNames(
        val language: AppLanguage,
        val previous: String,
        val next: String,
        val page: String,
        val childStatus: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            SwitcherNames(
                AppLanguage.ENGLISH,
                "Previous",
                "Next",
                "Page %1\$d",
                "Showing child %1\$d of %2\$d",
                "Previous and Next step the original container with its own transition. Copying carries the displayed child index.",
            ),
            SwitcherNames(
                AppLanguage.KOREAN,
                "이전",
                "다음",
                "페이지 %1\$d",
                "%2\$d개 중 %1\$d번째 자식 표시 중",
                "이전과 다음 버튼이 원본 컨테이너의 전환으로 이동합니다. 복사는 표시된 자식 인덱스를 옮깁니다.",
            ),
            SwitcherNames(
                AppLanguage.JAPANESE,
                "前へ",
                "次へ",
                "ページ %1\$d",
                "%2\$d個中%1\$d番目の子を表示中",
                "前へ/次へボタンが元のコンテナーの独自の遷移で移動します。コピーは表示中の子インデックスを移します。",
            ),
            SwitcherNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "上一页",
                "下一页",
                "第 %1\$d 页",
                "正在显示第 %1\$d 个子项，共 %2\$d 个",
                "“上一页”和“下一页”以原始容器自带的过渡效果切换。复制会转移当前显示的子项索引。",
            ),
            SwitcherNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "上一頁",
                "下一頁",
                "第 %1\$d 頁",
                "正在顯示第 %1\$d 個子項，共 %2\$d 個",
                "「上一頁」和「下一頁」以原始容器自帶的切換效果移動。複製會轉移目前顯示的子項索引。",
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
