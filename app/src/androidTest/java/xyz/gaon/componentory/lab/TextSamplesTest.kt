package xyz.gaon.componentory.lab

import android.graphics.Rect
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasNoClickAction
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
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click as nativeClick
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.navigation.ComponentoryApp
import xyz.gaon.componentory.settings.AppLanguage
import xyz.gaon.componentory.settings.LanguagePreferences

@RunWith(AndroidJUnit4::class)
class TextSamplesTest {
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
    fun material3TextKeepsLibraryTypographyWhenTheHostChangesItsFont() {
        val expected = androidx.compose.material3.Typography().bodyLarge
        val hostTypography =
            androidx.compose.material3.Typography(
                bodyLarge =
                    expected.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = 42.sp,
                        lineHeight = 64.sp,
                    )
            )
        compose.runOnUiThread {
            // Exercise the production renderer under a deliberately different caller theme.
            compose.activity.setContent {
                androidx.compose.material3.MaterialTheme(typography = hostTypography) {
                    Column(Modifier.width(360.dp).verticalScroll(rememberScrollState())) {
                        Material3Sample(LabComponent.TEXT, "LEFT", false, SampleState())
                    }
                }
            }
        }
        assertLibraryText("LEFT", english.fixture)
        val layouts = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag("library_LEFT").performSemanticsAction(
            SemanticsActions.GetTextLayoutResult
        ) { action ->
            assertTrue(action(layouts))
        }
        val actual = layouts.single().layoutInput.style
        assertEquals(expected.fontFamily, actual.fontFamily)
        assertEquals(expected.fontSize, actual.fontSize)
        assertEquals(expected.fontWeight, actual.fontWeight)
        assertEquals(expected.lineHeight, actual.lineHeight)
        assertEquals(expected.letterSpacing, actual.letterSpacing)
        assertEquals(expected.lineHeightStyle, actual.lineHeightStyle)
        assertEquals(expected.platformStyle, actual.platformStyle)
    }

    @Test
    fun allFiveTextProvidersRenderTheFixedFixtureWithoutInventingActionsOrLibraryDisabledState() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.TEXT, family, DesignFamily.MATERIAL3)
            assertIdentity("LEFT", family, "TextView")
            assertNativeTextDefaults("LEFT", family, english.fixture)
            touchNative("LEFT")
            assertNativeTextDefaults("LEFT", family, english.fixture)
            setEnabled(false)
            compose.runOnIdle { assertFalse(nativeText("LEFT").isEnabled) }
            assertNativeTextDefaults("LEFT", family, english.fixture)
            assertLibraryText("RIGHT", english.fixture)
            compose.onNodeWithTag("text_enabled_note_RIGHT").assertTextEquals(english.textNote)
        }
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            configure(LabComponent.TEXT, family, DesignFamily.MATERIAL3)
            assertIdentity("LEFT", family, "Text")
            assertLibraryText("LEFT", english.fixture)
            compose.onNodeWithTag("library_LEFT").performTouchInput { click() }
            assertLibraryText("LEFT", english.fixture)
            setEnabled(false)
            assertLibraryText("LEFT", english.fixture)
            compose.onNodeWithTag("text_enabled_note_LEFT").assertTextEquals(english.textNote)
        }
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_reason_LEFT_TO_RIGHT", useUnmergedTree = true)
            .assertTextEquals("This sample has no inputs to copy.")
        compose
            .onNodeWithTag("copy_setup_LEFT_TO_RIGHT")
            .performScrollTo()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertIsDisplayed()
        pressBack()
        assertLibraryText("LEFT", english.fixture)
        assertLibraryText("RIGHT", english.fixture)
    }

    @Test
    fun originalCheckedTextViewsStayReadOnlyWhileNamedHostConfigurationCopiesAndRestores() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.CHECKED_TEXT_VIEW, family, family)
            assertIdentity("LEFT", family, "CheckedTextView")
            assertChecked("LEFT", false, english)
            assertChecked("RIGHT", false, english)
            assertCheckMarkFixture("LEFT")
            touchNative("LEFT")
            assertChecked("LEFT", false, english)
            setChecked("LEFT", true, english.checkedLabel)
            assertChecked("LEFT", true, english)
            assertCheckMarkFixture("LEFT")
            assertChecked("RIGHT", false, english)
            touchNative("LEFT")
            assertChecked("LEFT", true, english)

            setEnabled(false)
            compose.runOnIdle { assertFalse(nativeText("LEFT").isEnabled) }
            setChecked("RIGHT", true, english.checkedLabel)
            setChecked("LEFT", false, english.checkedLabel)
            copyInputs("RIGHT_TO_LEFT")
            assertChecked("LEFT", true, english)
            assertChecked("RIGHT", true, english)
            compose.onNodeWithTag("enabled").assertIsOff()
            recreateActivity()
            assertChecked("LEFT", true, english)
            assertChecked("RIGHT", true, english)
            compose.runOnIdle { assertFalse(nativeText("LEFT").isEnabled) }
            setChecked("RIGHT", false, english.checkedLabel)
            assertChecked("LEFT", true, english)
            assertChecked("RIGHT", false, english)
            resetSamples()
            assertChecked("LEFT", false, english)
            assertChecked("RIGHT", false, english)
            setEnabled(true)
            compose.runOnIdle { assertTrue(nativeText("LEFT").isEnabled) }
        }
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        compose.onNodeWithTag("unsupported_LEFT").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("checked_text_config_LEFT").assertDoesNotExist()
        chooseFamily("LEFT", DesignFamily.MATERIAL)
        assertChecked("LEFT", false, english)
    }

    @Test
    fun fiveSettingsLanguagesNameRealCheckedConfigurationAndOriginalTextFixtures() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.TEXT, DesignFamily.MATERIAL2, DesignFamily.MATERIAL3)
            setEnabled(false)
            assertLibraryText("LEFT", text.fixture)
            assertLibraryText("RIGHT", text.fixture)
            compose.onNodeWithTag("text_enabled_note_LEFT").assertTextEquals(text.textNote)
            configure(LabComponent.CHECKED_TEXT_VIEW, DesignFamily.MATERIAL, DesignFamily.MATERIAL)
            assertChecked("LEFT", false, text)
            compose
                .onNodeWithTag("checked_text_configuration_LEFT")
                .assertTextEquals(text.checkedNote)
            setEnabled(false)
            setChecked("LEFT", true, text.checkedLabel)
            assertChecked("LEFT", true, text)
            assertChecked("RIGHT", false, text)
            touchNative("LEFT")
            assertChecked("LEFT", true, text)
            copyInputs("LEFT_TO_RIGHT")
            assertChecked("RIGHT", true, text)
            compose.onNodeWithTag("enabled").assertIsOff()
        }
    }

    @Test
    fun boundedHostKeepsDefaultMultilineTextAndCheckedSetupReachableAtLargeComposeFont() {
        // This changes only the real app's Compose host width/font scale. Native widgets keep
        // Android resource typography; it is not an OS resize, TalkBack or historical capture.
        compose.runOnUiThread {
            compose.activity.setContent {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale = 2f)
                ) {
                    Box(Modifier.width(360.dp)) { ComponentoryApp() }
                }
            }
        }
        compose.waitForIdle()
        compose.onNodeWithTag("nav_compare").performClick()
        configure(LabComponent.TEXT, DesignFamily.MATERIAL, DesignFamily.MATERIAL3)
        assertNativeTextDefaults("LEFT", DesignFamily.MATERIAL, english.fixture)
        assertLibraryText("RIGHT", english.fixture, minimumLines = 4)
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        assertLibraryText("LEFT", english.fixture, minimumLines = 4)
        configure(LabComponent.CHECKED_TEXT_VIEW, DesignFamily.CLASSIC, DesignFamily.HOLO)
        setEnabled(false)
        setChecked("LEFT", true, english.checkedLabel)
        copyInputs("LEFT_TO_RIGHT")
        assertChecked("LEFT", true, english)
        assertChecked("RIGHT", true, english)
        val host = compose.onNodeWithTag("compare_screen").fetchSemanticsNode().boundsInRoot
        val control =
            compose.onNodeWithTag("checked_text_config_RIGHT").performScrollTo().assertIsDisplayed()
        val bounds = control.fetchSemanticsNode().boundsInRoot
        assertTrue(
            "The named host control must stay within the bounded app",
            bounds.width > 0f && bounds.left >= host.left && bounds.right <= host.right,
        )
        resetSamples()
        assertChecked("LEFT", false, english)
        assertChecked("RIGHT", false, english)
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

    private fun assertIdentity(panel: String, family: DesignFamily, api: String) {
        val packageName =
            when (family) {
                DesignFamily.MATERIAL2 -> "androidx.compose.material"
                DesignFamily.MATERIAL3 -> "androidx.compose.material3"
                else -> "android.widget"
            }
        compose.onNodeWithTag("source_$panel").assertTextEquals("$packageName.$api")
        val implementation =
            when (family) {
                DesignFamily.MATERIAL2 ->
                    "androidx.compose.material:material:${BuildConfig.MATERIAL2_VERSION}"
                DesignFamily.MATERIAL3 ->
                    "androidx.compose.material3:material3:${BuildConfig.MATERIAL3_VERSION}"
                else -> "android:${requireNotNull(family.platform).themeName}"
            }
        compose.onNodeWithTag("implementation_$panel").assertTextContains(implementation)
    }

    private fun nativeText(panel: String): TextView = compose.activity.findViewById(nativeId(panel))

    private fun assertNativeTextDefaults(panel: String, family: DesignFamily, fixture: String) {
        compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            val actual = nativeText(panel)
            assertEquals(TextView::class.java, actual.javaClass)
            assertEquals(ContextThemeWrapper::class.java, actual.context.javaClass)
            val themed =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId)
            val reference = TextView(themed).apply { isEnabled = actual.isEnabled }
            val expectedStyle = TypedValue()
            val actualStyle = TypedValue()
            assertEquals(
                themed.theme.resolveAttribute(android.R.attr.textViewStyle, expectedStyle, true),
                actual.context.theme.resolveAttribute(
                    android.R.attr.textViewStyle,
                    actualStyle,
                    true,
                ),
            )
            assertEquals(expectedStyle.resourceId, actualStyle.resourceId)
            // Constructor metrics and public defaults are not pixel or historical appearance proof.
            assertEquals(reference.textSize, actual.textSize, 0f)
            assertEquals(reference.typeface, actual.typeface)
            assertEquals(reference.currentTextColor, actual.currentTextColor)
            assertEquals(reference.gravity, actual.gravity)
            assertEquals(reference.includeFontPadding, actual.includeFontPadding)
            assertEquals(reference.lineSpacingExtra, actual.lineSpacingExtra, 0f)
            assertEquals(reference.lineSpacingMultiplier, actual.lineSpacingMultiplier, 0f)
            assertEquals(fixture, actual.text.toString())
            assertFalse(actual.isClickable)
            assertFalse(actual.hasOnClickListeners())
            assertNativeLayout(actual)
        }
    }

    private fun assertNativeLayout(view: TextView) {
        val visible = Rect()
        assertTrue(
            view.isShown && view.width > 0 && view.height > 0 && view.getGlobalVisibleRect(visible)
        )
        assertTrue(visible.width() > 0 && visible.height() > 0)
        val layout = requireNotNull(view.layout)
        assertTrue(
            "The original view must lay out the fixed multiline fixture",
            layout.lineCount >= 3,
        )
        repeat(layout.lineCount) { line ->
            assertEquals(
                "The original default text must not ellipsize a fixture line",
                0,
                layout.getEllipsisCount(line),
            )
        }
    }

    private fun assertLibraryText(panel: String, fixture: String, minimumLines: Int = 3) {
        val sample = compose.onNodeWithTag("library_$panel").performScrollTo().assertIsDisplayed()
        sample
            .assertTextEquals(fixture)
            .assertHasNoClickAction()
            .assertIsEnabled()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
        val layouts = mutableListOf<TextLayoutResult>()
        sample.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts))
        }
        val layout = layouts.single()
        assertEquals(fixture, layout.layoutInput.text.text)
        assertTrue(
            "Original library Text must wrap the complete fixture",
            layout.lineCount >= minimumLines,
        )
        assertFalse(layout.hasVisualOverflow)
        val bounds = sample.fetchSemanticsNode().boundsInRoot
        assertTrue(bounds.width > 0f && bounds.height > 0f)
    }

    // The Boolean accessibility API also works on API 24–35, before the newer checked-state API.
    @Suppress("DEPRECATION")
    private fun assertChecked(panel: String, expected: Boolean, text: TextNames) {
        compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            val view = nativeText(panel) as CheckedTextView
            assertEquals(CheckedTextView::class.java, view.javaClass)
            assertEquals(text.fixture, view.text.toString())
            assertEquals(expected, view.isChecked)
            assertFalse(view.isClickable)
            assertFalse(view.hasOnClickListeners())
            val node = view.createAccessibilityNodeInfo()
            assertEquals("android.widget.CheckedTextView", node.className.toString())
            assertTrue(node.isCheckable)
            assertEquals(expected, node.isChecked)
            assertFalse(node.isClickable)
            assertNativeLayout(view)
        }
        compose
            .onNodeWithTag("status_$panel")
            .assertTextEquals(if (expected) text.checked else text.unchecked)
        val control = compose.onNodeWithTag("checked_text_config_$panel")
        control
            .assertContentDescriptionEquals(text.checkedLabel)
            .assertIsEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        if (expected) control.assertIsOn() else control.assertIsOff()
    }

    private fun assertCheckMarkFixture(panel: String) {
        val missing =
            compose.runOnIdle {
                val view = nativeText(panel) as CheckedTextView
                val attributes =
                    view.context.obtainStyledAttributes(
                        intArrayOf(android.R.attr.listChoiceIndicatorMultiple)
                    )
                try {
                    val reference = attributes.getDrawable(0)
                    if (reference == null) {
                        assertNull(view.checkMarkDrawable)
                        true
                    } else {
                        val actual = requireNotNull(view.checkMarkDrawable)
                        reference.state = view.drawableState
                        assertEquals(reference.javaClass, actual.javaClass)
                        assertEquals(reference.intrinsicWidth, actual.intrinsicWidth)
                        assertEquals(reference.intrinsicHeight, actual.intrinsicHeight)
                        assertEquals(
                            view.isChecked,
                            actual.state.contains(android.R.attr.state_checked),
                        )
                        false
                    }
                } finally {
                    attributes.recycle()
                }
            }
        if (missing)
            compose
                .onNodeWithTag("checked_text_missing_mark_$panel")
                .assertTextEquals(compose.activity.getString(R.string.checked_text_missing_mark))
        else compose.onNodeWithTag("checked_text_missing_mark_$panel").assertDoesNotExist()
    }

    private fun setChecked(panel: String, checked: Boolean, label: String) {
        val control =
            compose
                .onNodeWithTag("checked_text_config_$panel")
                .performScrollTo()
                .assertIsDisplayed()
        control.assertContentDescriptionEquals(label).assertIsEnabled()
        val current =
            control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On
        if (current != checked) control.performTouchInput { click() }
        if (checked) control.assertIsOn() else control.assertIsOff()
    }

    private fun touchNative(panel: String) {
        compose.onNodeWithTag("native_$panel").performScrollTo().assertIsDisplayed()
        onView(withId(nativeId(panel))).perform(nativeClick())
        compose.waitForIdle()
    }

    private fun copyInputs(direction: String) {
        touchTag("copy_inputs")
        compose
            .onNodeWithTag("copy_setup_$direction")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performTouchInput { click() }
        compose.onNodeWithTag("copy_inputs_menu").assertDoesNotExist()
        compose.onNodeWithTag("copy_setup_result").assertExists()
    }

    private fun setEnabled(enabled: Boolean) {
        val control = compose.onNodeWithTag("enabled").performScrollTo().assertIsDisplayed()
        val current =
            control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On
        if (current != enabled) control.performTouchInput { click() }
        if (enabled) control.assertIsOn() else control.assertIsOff()
    }

    private fun resetSamples() = touchTag("reset")

    private fun touchTag(tag: String) {
        compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performTouchInput {
            click()
        }
    }

    private fun nativeId(panel: String) =
        if (panel == "LEFT") R.id.sample_left else R.id.sample_right

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

    private data class TextNames(
        val language: AppLanguage,
        val fixture: String,
        val checkedLabel: String,
        val checked: String,
        val unchecked: String,
        val textNote: String,
        val checkedNote: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)
    private val names =
        listOf(
            TextNames(
                AppLanguage.ENGLISH,
                "Hello, Android.\nThis sample uses several lines.\nCompare text appearance and spacing.",
                "Checked",
                "Checked",
                "Unchecked",
                "Enabled does not apply to library Text. Its API has no enabled parameter.",
                "Uses the theme's multiple-choice check mark when available. Use Checked to change the state. Tapping the text does not toggle it.",
            ),
            TextNames(
                AppLanguage.KOREAN,
                "안녕하세요, Android.\n여러 줄로 표시한 샘플입니다.\n글자 모양과 줄 간격을 비교하세요.",
                "체크 상태",
                "선택됨",
                "선택 해제됨",
                "라이브러리 Text에는 활성화 설정이 적용되지 않습니다. 이 API에는 활성화 매개변수가 없습니다.",
                "테마의 다중 선택 표시가 있으면 사용합니다. 체크 상태 설정으로 상태를 변경하세요. 텍스트를 눌러도 자동으로 바뀌지 않습니다.",
            ),
            TextNames(
                AppLanguage.JAPANESE,
                "こんにちは、Android。\nこのサンプルは複数行で表示されます。\n文字の表示と間隔を比較しましょう。",
                "チェック状態",
                "チェック済み",
                "未チェック",
                "ライブラリの Text には有効・無効の設定が適用されません。この API には有効・無効を指定する引数がありません。",
                "テーマの複数選択用チェックマークがある場合は使用します。チェック状態の設定で状態を変更してください。テキストを押しても自動では切り替わりません。",
            ),
            TextNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "你好，Android。\n此示例包含多行文字。\n比较文字外观和行间距。",
                "勾选状态",
                "已勾选",
                "未勾选",
                "启用设置不适用于库的 Text。此 API 没有启用参数。",
                "主题提供多选勾选标记时会使用该标记。请通过勾选状态设置更改状态。点击文本不会自动切换状态。",
            ),
            TextNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "你好，Android。\n此範例包含多行文字。\n比較文字外觀和行距。",
                "勾選狀態",
                "已勾選",
                "未勾選",
                "啟用設定不適用於程式庫的 Text。此 API 沒有啟用參數。",
                "主題提供多選勾選標記時會使用該標記。請透過勾選狀態設定變更狀態。點選文字不會自動切換狀態。",
            ),
        )
    private val english
        get() = names.first()
}
