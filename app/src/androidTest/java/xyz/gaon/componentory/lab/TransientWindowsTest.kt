package xyz.gaon.componentory.lab

import android.app.Dialog
import android.app.ProgressDialog
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class TransientWindowsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareEnglishComparison() {
        keepScreenOn()
        changeLanguage(AppLanguage.ENGLISH)
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @After
    fun restoreEnglishBaseline() {
        compose.runOnIdle {
            (trigger("LEFT").tag as? Dialog)?.dismiss()
            (trigger("RIGHT").tag as? Dialog)?.dismiss()
        }
        compose.runOnUiThread { LanguagePreferences.apply(compose.activity, AppLanguage.ENGLISH) }
        compose.waitForIdle()
        keepScreenOn()
    }

    @Test
    fun plainDialogOpensTheRealBaseDialogAndCountsOpens() {
        nativeFamilies.forEach { family ->
            configure(LabComponent.PLAIN_DIALOG, family, DesignFamily.MATERIAL3)
            assertNativeIdentity("LEFT", LabComponent.PLAIN_DIALOG, family)
            status("LEFT", "Shown 0 times")
            tapTrigger("LEFT")
            compose.runOnIdle {
                val dialog = trigger("LEFT").tag as Dialog
                // The plain Dialog, not the AlertDialog subclass.
                assertEquals(Dialog::class.java, dialog.javaClass)
                assertTrue(dialog.isShowing)
                assertNotNull(dialog.window)
                dialog.dismiss()
                assertFalse(dialog.isShowing)
            }
            status("LEFT", "Shown 1 times")
            tapTrigger("LEFT")
            compose.runOnIdle { (trigger("LEFT").tag as Dialog).dismiss() }
            status("LEFT", "Shown 2 times")
        }
    }

    @Test
    fun progressDialogShowsTheRealDeprecatedDialog() {
        configure(LabComponent.PROGRESS_DIALOG, DesignFamily.HOLO, DesignFamily.MATERIAL3)
        assertNativeIdentity("LEFT", LabComponent.PROGRESS_DIALOG, DesignFamily.HOLO)
        compose
            .onNodeWithTag("clock_note_LEFT")
            .performScrollTo()
            .assertTextContains("Deprecated since API 26", substring = true)
        tapTrigger("LEFT")
        compose.runOnIdle {
            val dialog = trigger("LEFT").tag as ProgressDialog
            assertEquals(ProgressDialog::class.java, dialog.javaClass)
            assertTrue(dialog.isShowing)
            assertTrue(dialog.isIndeterminate)
            dialog.dismiss()
        }
        status("LEFT", "Shown 1 times")
        setEnabled(false)
        compose.runOnIdle { assertFalse(trigger("LEFT").isEnabled) }
        onView(withId(nativeId("LEFT"))).perform(nativeClick())
        // The disabled trigger cannot open a second window or bump the count.
        status("LEFT", "Shown 1 times")
    }

    @Test
    fun toastShowsTheRealToastAndCountsOpens() {
        configure(LabComponent.TOAST, DesignFamily.CLASSIC, DesignFamily.HOLO)
        assertNativeIdentity("LEFT", LabComponent.TOAST, DesignFamily.CLASSIC)
        status("LEFT", "Shown 0 times")
        tapTrigger("LEFT")
        compose.runOnIdle { assertEquals(Toast::class.java, trigger("LEFT").tag!!.javaClass) }
        status("LEFT", "Shown 1 times")
        tapTrigger("LEFT")
        status("LEFT", "Shown 2 times")
    }

    @Test
    fun transientCopiesCarryTheOpenCountAndRecreationRestoresIt() {
        configure(LabComponent.PLAIN_DIALOG, DesignFamily.CLASSIC, DesignFamily.HOLO)
        tapTrigger("LEFT")
        compose.runOnIdle { (trigger("LEFT").tag as Dialog).dismiss() }
        tapTrigger("LEFT")
        compose.runOnIdle { (trigger("LEFT").tag as Dialog).dismiss() }
        status("LEFT", "Shown 2 times")
        copyInputs("LEFT_TO_RIGHT")
        status("RIGHT", "Shown 2 times")
        compose.runOnIdle { assertNull(trigger("RIGHT").tag as? Dialog) }

        recreateActivity()
        status("LEFT", "Shown 2 times")
        status("RIGHT", "Shown 2 times")

        configure(LabComponent.TOAST, DesignFamily.CLASSIC, DesignFamily.MATERIAL2)
        compose
            .onNodeWithTag("unsupported_RIGHT")
            .performScrollTo()
            .assertIsDisplayed()
            .assertTextContains("The Material 2 library does not provide Toast.", substring = true)
        blockedCopy("LEFT_TO_RIGHT", "The target provider does not support this sample.")
    }

    @Test
    fun fiveLanguagesLocalizeTransientLabelsNotesAndButtons() {
        names.forEach { text ->
            changeLanguage(text.language)
            compose.onNodeWithTag("nav_compare").performClick()
            configure(LabComponent.PLAIN_DIALOG, DesignFamily.CLASSIC, DesignFamily.MATERIAL3)
            status("LEFT", String.format(text.shown, 0))
            compose.onNodeWithTag("transient_note_LEFT").assertTextEquals(text.note)
            compose.runOnIdle { assertEquals(text.open, trigger("LEFT").text.toString()) }
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

    private fun trigger(panel: String): Button = compose.activity.findViewById(nativeId(panel))

    private fun tapTrigger(panel: String) {
        onView(withId(nativeId(panel))).perform(nativeClick())
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
            val view = trigger(panel)
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

    private data class TransientNames(
        val language: AppLanguage,
        val open: String,
        val shown: String,
        val note: String,
    )

    private val nativeFamilies =
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL)

    private val names =
        listOf(
            TransientNames(
                AppLanguage.ENGLISH,
                "Open dialog",
                "Shown %1\$d times",
                "The button opens the real transient window. Copying carries only the count of opens.",
            ),
            TransientNames(
                AppLanguage.KOREAN,
                "대화상자 열기",
                "%1\$d번 표시됨",
                "버튼이 실제 일시적 창을 엽니다. 복사는 열린 횟수만 옮깁니다.",
            ),
            TransientNames(
                AppLanguage.JAPANESE,
                "ダイアログを開く",
                "%1\$d回表示",
                "ボタンが実際の一時的なウィンドウを開きます。コピーは開いた回数のみを移します。",
            ),
            TransientNames(
                AppLanguage.SIMPLIFIED_CHINESE,
                "打开对话框",
                "已显示 %1\$d 次",
                "按钮打开真实的瞬时窗口。复制仅转移打开次数。",
            ),
            TransientNames(
                AppLanguage.TRADITIONAL_CHINESE,
                "開啟對話框",
                "已顯示 %1\$d 次",
                "按鈕開啟真實的暫時性視窗。複製僅轉移開啟次數。",
            ),
        )
}
