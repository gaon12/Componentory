package xyz.gaon.componentory.lab

import android.os.SystemClock
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AnalogClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.DigitalClock
import android.widget.TextClock
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
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
import org.junit.Assert.assertNotEquals
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

// The ticking clocks use real sleeps; an indeterminate view is never idled on.
@RunWith(AndroidJUnit4::class)
@Suppress("DEPRECATION")
class ClockSamplesTest {
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
    fun nativeClockDisplaysUseOriginalWidgetsFormatsAndEnabledState() {
        nativeFamilies.forEach { family ->
            mapOf(
                    LabComponent.TEXT_CLOCK to TextClock::class.java,
                    LabComponent.ANALOG_CLOCK to AnalogClock::class.java,
                    LabComponent.DIGITAL_CLOCK to DigitalClock::class.java,
                )
                .forEach { (component, type) ->
                    configure(component, family, DesignFamily.MATERIAL3)
                    assertNativeIdentity("LEFT", component, family)
                    compose.runOnIdle {
                        val view = nativeClock("LEFT")
                        assertEquals(type, view.javaClass)
                        assertTrue(
                            view.isShown && view.width > 0 && view.height > 0 && view.isEnabled
                        )
                    }
                    if (component == LabComponent.TEXT_CLOCK) {
                        assertTextClockFormat("LEFT", true)
                        status("LEFT", "Clock format: 24-hour")
                        setFormat("LEFT", false)
                        assertTextClockFormat("LEFT", false)
                        status("LEFT", "Clock format: 12-hour")
                        compose
                            .onNodeWithTag("clock_note_LEFT")
                            .assertTextEquals(clockNotes.textClock)
                        setFormat("LEFT", true)
                    } else {
                        status("LEFT", "Preview: ${component.label}")
                        compose
                            .onNodeWithTag("clock_note_LEFT")
                            .assertTextEquals(
                                deprecatedNote(
                                    AppLanguage.ENGLISH,
                                    if (component == LabComponent.ANALOG_CLOCK) 23 else 17,
                                )
                            )
                    }
                    setEnabled(false)
                    compose.runOnIdle { assertFalse(nativeClock("LEFT").isEnabled) }
                }
        }
    }

    @Test
    fun chronometerButtonsDriveTheOriginalTimerAndSurviveRecreation() {
        configure(LabComponent.CHRONOMETER, DesignFamily.MATERIAL, DesignFamily.MATERIAL)
        assertNativeIdentity("LEFT", LabComponent.CHRONOMETER, DesignFamily.MATERIAL)
        compose.runOnIdle {
            val clock = chronometer("LEFT")
            assertTrue(clock.text.toString().matches(Regex("\\d{2}:\\d{2}")))
            assertEquals("00:00", clock.text.toString())
        }
        status("LEFT", "Chronometer: stopped at 0:00:00")
        tapChronometer("LEFT", 0)
        status("LEFT", "Chronometer: running")
        val before = compose.runOnIdle { chronometer("LEFT").text.toString() }
        SystemClock.sleep(1200)
        compose.runOnIdle { assertNotEquals(before, chronometer("LEFT").text.toString()) }
        tapChronometer("LEFT", 1)
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextContains("Chronometer: stopped at", substring = true)
        val frozen = compose.runOnIdle { chronometer("LEFT").text.toString() }
        SystemClock.sleep(1200)
        compose.runOnIdle {
            assertEquals(
                "A stopped chronometer must not tick",
                frozen,
                chronometer("LEFT").text.toString(),
            )
        }
        recreateActivity()
        compose.runOnIdle {
            assertEquals(
                "A stopped chronometer keeps its frozen display",
                frozen,
                chronometer("LEFT").text.toString(),
            )
        }
        tapChronometer("LEFT", 0)
        status("LEFT", "Chronometer: running")
        recreateActivity()
        status("LEFT", "Chronometer: running")
        SystemClock.sleep(1200)
        compose.runOnIdle {
            assertTrue(
                "A recreated running chronometer keeps counting",
                chronometer("LEFT").text.toString() != "0:00",
            )
        }
        setEnabled(false)
        compose.runOnIdle {
            assertFalse(chronometer("LEFT").isEnabled)
            (0..2).forEach { assertFalse(chronometerControl("LEFT", it).isEnabled) }
        }
        setEnabled(true)
        tapChronometer("LEFT", 2)
        status("LEFT", "Chronometer: running")
        tapChronometer("LEFT", 1)
        compose.onNodeWithTag("clock_note_LEFT").assertTextEquals(clockNotes.chronometer)
    }

    @Test
    fun clockCopiesCarryOnlyTheFormatOrRunningStateTheirPanelOwns() {
        configure(LabComponent.TEXT_CLOCK, DesignFamily.CLASSIC, DesignFamily.HOLO)
        setFormat("LEFT", false)
        copyInputs("LEFT_TO_RIGHT")
        assertTextClockFormat("RIGHT", false)
        status("RIGHT", "Clock format: 12-hour")
        status("LEFT", "Clock format: 12-hour")

        configure(LabComponent.CHRONOMETER, DesignFamily.HOLO, DesignFamily.CLASSIC)
        tapChronometer("LEFT", 0)
        SystemClock.sleep(1100)
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Chronometer: running")
        val seen = compose.runOnIdle { chronometer("RIGHT").text.toString() }
        SystemClock.sleep(1200)
        compose.runOnIdle {
            assertNotEquals(
                "A copied running chronometer resumes on the target",
                seen,
                chronometer("RIGHT").text.toString(),
            )
        }

        configure(LabComponent.ANALOG_CLOCK, DesignFamily.CLASSIC, DesignFamily.HOLO)
        blockedCopy("LEFT_TO_RIGHT", "This sample has no inputs to copy.")

        configure(LabComponent.TEXT_CLOCK, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .assertTextContains(
                "The Material 2 library does not provide Text clock.",
                substring = true,
            )
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
        compose.onNodeWithTag("unsupported_RIGHT").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun fiveLanguagesLocalizeClockLabelsNotesAndChronometerButtons() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.ANALOG_CLOCK, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", "${text.preview}: ${text.analogLabel}")
            compose
                .onNodeWithTag("clock_note_LEFT")
                .assertTextEquals(deprecatedNote(text.language, 23))
            configure(LabComponent.CHRONOMETER, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", text.stoppedZero)
            compose.runOnIdle {
                assertEquals(text.start, chronometerControl("LEFT", 0).text.toString())
                assertEquals(text.stop, chronometerControl("LEFT", 1).text.toString())
                assertEquals(text.reset, chronometerControl("LEFT", 2).text.toString())
            }
            compose.onNodeWithTag("clock_note_LEFT").assertTextEquals(text.chronometerNote)
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

    private fun nativeClock(panel: String): View = compose.activity.findViewById(nativeId(panel))

    private fun chronometer(panel: String): Chronometer = nativeClock(panel) as Chronometer

    private fun chronometerControl(panel: String, index: Int): Button {
        val root = chronometer(panel).parent as ViewGroup
        return (root.getChildAt(1) as ViewGroup).getChildAt(index) as Button
    }

    private fun tapChronometer(panel: String, index: Int) {
        val action = listOf("start", "stop", "reset")[index]
        onView(
                allOf(
                    withTagValue(equalTo("chronometer_${action}_$panel")),
                    isAssignableFrom(Button::class.java),
                )
            )
            .perform(nativeClick())
        compose.waitForIdle()
    }

    private fun assertTextClockFormat(panel: String, use24: Boolean) {
        val pattern = if (use24) "HH:mm:ss" else "h:mm:ss a"
        compose.runOnIdle {
            val clock = nativeClock(panel) as TextClock
            assertEquals(pattern, clock.format24Hour.toString())
            assertEquals(pattern, clock.format12Hour.toString())
        }
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
            val view = nativeClock(panel)
            assertEquals(ContextThemeWrapper::class.java, view.context.javaClass)
            val expected = TypedValue()
            val actual = TypedValue()
            val theme =
                ContextThemeWrapper(compose.activity, requireNotNull(family.platform).themeId).theme
            assertEquals(
                theme.resolveAttribute(android.R.attr.textViewStyle, expected, true),
                view.context.theme.resolveAttribute(android.R.attr.textViewStyle, actual, true),
            )
            assertEquals(expected.resourceId, actual.resourceId)
            assertEquals(expected.data, actual.data)
        }
    }

    private fun status(panel: String, expected: String) {
        compose.onNodeWithTag("status_$panel").performScrollTo().assertTextEquals(expected)
    }

    private fun setFormat(panel: String, use24: Boolean) {
        val control =
            compose.onNodeWithTag("time_24_hour_$panel").performScrollTo().assertIsDisplayed()
        control
            .assertIsEnabled()
            .assertContentDescriptionEquals(clockNotes.format24)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
        if (
            (control.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On) != use24
        )
            control.performTouchInput { click() }
        if (use24) control.assertIsOn() else control.assertIsOff()
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

    private data class ClockNames(
        val language: AppLanguage,
        val preview: String,
        val analogLabel: String,
        val start: String,
        val stop: String,
        val reset: String,
        val chronometerNote: String,
        val stoppedZero: String,
    )

    private fun deprecatedNote(language: AppLanguage, api: Int): String =
        when (language) {
            AppLanguage.ENGLISH ->
                "Deprecated since API $api. The framework still supplies and renders it; Compose libraries have no replacement."
            AppLanguage.KOREAN ->
                "API ${api}부터 지원 중단됐습니다. 프레임워크는 계속 제공하고 그리지만 Compose 라이브러리에는 대체품이 없습니다."
            AppLanguage.JAPANESE ->
                "API ${api}で非推奨になりました。フレームワークは引き続き提供・描画しますが、Composeライブラリに代替はありません。"
            AppLanguage.SIMPLIFIED_CHINESE -> "自 API ${api} 起已废弃。框架仍会提供并渲染它；Compose 库中没有替代品。"
            AppLanguage.TRADITIONAL_CHINESE -> "自 API ${api} 起已棄用。架構仍會提供並繪製它；Compose 函式庫中沒有替代品。"
            else -> error("Unsupported language $language")
        }

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private data class ClockNotes(
        val textClock: String,
        val format24: String,
        val chronometer: String,
    )

    private val clockNotes =
        ClockNotes(
            textClock =
                "Shows the current device time and updates itself. The 24-hour switch pins both format fields so the display follows this sample, not the system setting.",
            format24 = "24-hour clock",
            chronometer =
                "Start, Stop and Reset drive the original Chronometer. A running timer keeps counting and resumes after recreation; Stop freezes the elapsed time.",
        )

    private val names =
        listOf(
            ClockNames(
                AppLanguage.ENGLISH,
                "Preview",
                "Analog clock",
                "Start",
                "Stop",
                "Reset",
                "Start, Stop and Reset drive the original Chronometer. A running timer keeps counting and resumes after recreation; Stop freezes the elapsed time.",
                "Chronometer: stopped at 0:00:00",
            ),
            ClockNames(
                AppLanguage.KOREAN,
                "미리보기",
                "아날로그 시계",
                "시작",
                "정지",
                "초기화",
                "시작, 정지, 초기화 버튼이 원본 Chronometer를 구동합니다. 실행 중인 타이머는 계속 세며 화면 회전 후에도 이어집니다. 정지하면 경과 시간이 고정됩니다.",
                "크로노미터: 0:00:00에서 정지",
            ),
            ClockNames(
                AppLanguage.JAPANESE,
                "プレビュー",
                "アナログ時計",
                "開始",
                "停止",
                "リセット",
                "開始、停止、リセットが元のChronometerを動かします。実行中のタイマーはカウントを続け、画面回転後も再開します。停止すると経過時間が固定されます。",
                "クロノメーター: 0:00:00で停止",
            ),
            ClockNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "预览",
                "模拟时钟",
                "开始",
                "停止",
                "重置",
                "开始、停止和重置按钮驱动原始 Chronometer。运行中的计时器会持续计时并在重建后恢复；停止会冻结已计时间。",
                "计时器: 已停止于 0:00:00",
            ),
            ClockNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "預覽",
                "類比時鐘",
                "開始",
                "停止",
                "重設",
                "開始、停止和重設按鈕驅動原始 Chronometer。執行中的計時器會持續計時並在重建後恢復；停止會凍結已計時間。",
                "計時器: 已停止於 0:00:00",
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
