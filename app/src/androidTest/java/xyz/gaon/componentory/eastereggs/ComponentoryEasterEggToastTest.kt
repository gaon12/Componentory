package xyz.gaon.componentory.eastereggs

import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class ComponentoryEasterEggToastTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openSettings() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("app_version").performScrollTo()
    }

    @Test
    fun nativeToastsCountDownFromTheThirdTapAndTheSeventhOpensTheScreen() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        tapVersion(2)
        compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        for (remaining in 4 downTo 1) {
            val expected =
                if (remaining == 1) "1 more tap to open the Easter egg."
                else "$remaining more taps to open the Easter egg."
            val event =
                automation.executeAndWaitForEvent(
                    { tapVersion(1) },
                    {
                        it.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED &&
                            it.text.any { text -> text.toString() == expected }
                    },
                    5_000,
                )
            assertTrue(event.text.any { it.toString() == expected })
            compose.onNodeWithTag("componentory_easter_egg").assertDoesNotExist()
        }
        tapVersion(1)
        compose.onNodeWithTag("componentory_easter_egg").assertIsDisplayed()
    }

    private fun tapVersion(count: Int) {
        compose.onNodeWithTag("app_version").performTouchInput { repeat(count) { click() } }
    }
}
