package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Gallery
import android.widget.SlidingDrawer
import android.widget.TextView
import android.widget.TwoLineListItem
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
class LegacyContainersTest {
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
    fun nativeLegacyContainersUseTheRealFrameworkWidgets() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.GALLERY, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.GALLERY, family)
            compose.runOnIdle {
                val gallery = view("LEFT") as Gallery
                assertEquals(6, gallery.adapter.count)
                assertEquals(0, gallery.selectedItemPosition)
            }
            status("LEFT", "Selected item 1 of 6")

            configure(LabComponent.SLIDING_DRAWER, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.SLIDING_DRAWER, family)
            compose.runOnIdle {
                val drawer = view("LEFT") as SlidingDrawer
                assertFalse(drawer.isOpened)
                assertNotNull(drawer.findViewById<View>(R.id.sliding_handle))
                assertNotNull(drawer.findViewById<View>(R.id.sliding_content))
            }
            status("LEFT", "Drawer closed")

            configure(LabComponent.TWO_LINE_LIST_ITEM, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.TWO_LINE_LIST_ITEM, family)
            compose.runOnIdle {
                val item = view("LEFT") as TwoLineListItem
                assertEquals(
                    compose.activity.getString(R.string.two_line_primary),
                    item.findViewById<TextView>(android.R.id.text1).text.toString(),
                )
                assertEquals(
                    compose.activity.getString(R.string.two_line_secondary),
                    item.findViewById<TextView>(android.R.id.text2).text.toString(),
                )
            }
            status("LEFT", "Preview: Two-line list item")
            compose
                .onNodeWithTag("legacy_note_LEFT")
                .performScrollTo()
                .assertTextContains("deprecated", substring = true)
        }
    }

    @Test
    fun galleryAndDrawerStateCopyAndSurviveRecreation() {
        configure(LabComponent.GALLERY, DesignFamily.CLASSIC, DesignFamily.HOLO)
        selectGalleryItem("LEFT", 4)
        status("LEFT", "Selected item 5 of 6")
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle { assertEquals(4, (view("RIGHT") as Gallery).selectedItemPosition) }
        status("RIGHT", "Selected item 5 of 6")
        status("LEFT", "Selected item 5 of 6")

        recreateActivity()
        compose.runOnIdle {
            assertEquals(4, (view("LEFT") as Gallery).selectedItemPosition)
            assertEquals(4, (view("RIGHT") as Gallery).selectedItemPosition)
        }

        configure(LabComponent.SLIDING_DRAWER, DesignFamily.CLASSIC, DesignFamily.HOLO)
        tapDrawerHandle("LEFT")
        status("LEFT", "Drawer open")
        copyInputs("LEFT_TO_RIGHT")
        compose.runOnIdle { assertTrue((view("RIGHT") as SlidingDrawer).isOpened) }
        status("RIGHT", "Drawer open")

        recreateActivity()
        compose.runOnIdle {
            assertTrue((view("LEFT") as SlidingDrawer).isOpened)
            assertTrue((view("RIGHT") as SlidingDrawer).isOpened)
        }
    }

    @Test
    fun disabledLegacyContainersStopRespondingAndLibraryCellsExplainThemselves() {
        configure(LabComponent.SLIDING_DRAWER, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        setEnabled(false)
        // Both panels carry the same handle id, so tap the left one directly.
        compose.runOnIdle {
            (view("LEFT") as SlidingDrawer).findViewById<Button>(R.id.sliding_handle).performClick()
        }
        compose.waitForIdle()
        compose.runOnIdle { assertFalse((view("LEFT") as SlidingDrawer).isOpened) }
        status("LEFT", "Drawer closed")
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 2 library does not provide Sliding drawer.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")

        configure(LabComponent.GALLERY, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
        setEnabled(false)
        compose.runOnIdle { assertFalse((view("LEFT") as Gallery).isEnabled) }
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains(
                "The Material 3 library does not provide Gallery.",
                substring = true,
            )
    }

    @Test
    fun fiveLanguagesLocalizeLegacyLabelsNotesAndStatuses() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.SLIDING_DRAWER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", text.closed)
            compose.onNodeWithTag("legacy_note_LEFT").assertTextEquals(text.note)
            tapDrawerHandle("LEFT")
            status("LEFT", text.opened)
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

    private fun selectGalleryItem(panel: String, index: Int) {
        compose.runOnIdle { (view(panel) as Gallery).setSelection(index, false) }
        compose.waitForIdle()
    }

    private fun tapDrawerHandle(panel: String) {
        compose.runOnIdle {
            (view(panel) as SlidingDrawer).findViewById<Button>(R.id.sliding_handle).performClick()
        }
        compose.waitUntil(10_000) { (view(panel) as SlidingDrawer).isOpened }
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

    private data class LegacyNames(
        val language: AppLanguage,
        val closed: String,
        val opened: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            LegacyNames(
                AppLanguage.ENGLISH,
                "Drawer closed",
                "Drawer open",
                "The widget is deprecated but still renders. Copying carries only the visible selection.",
            ),
            LegacyNames(
                AppLanguage.KOREAN,
                "드로어 닫힘",
                "드로어 열림",
                "위젯은 지원 중단됐지만 여전히 렌더링됩니다. 복사는 보이는 선택만 옮깁니다.",
            ),
            LegacyNames(
                AppLanguage.JAPANESE,
                "ドロワーが閉じています",
                "ドロワーが開いています",
                "ウィジェットは非推奨ですが引き続きレンダリングされます。コピーは表示中の選択のみを移します。",
            ),
            LegacyNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "抽屉已关闭",
                "抽屉已打开",
                "该小部件已弃用但仍可渲染。复制仅转移当前可见的选择。",
            ),
            LegacyNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "抽屜已關閉",
                "抽屜已開啟",
                "小工具已棄用但仍可渲染。複製僅轉移目前可見的選擇。",
            ),
        )
}
