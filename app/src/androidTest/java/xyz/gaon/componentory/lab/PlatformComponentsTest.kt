package xyz.gaon.componentory.lab

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class PlatformComponentsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareLab() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("runtime").assertExists()
    }

    @Test
    fun selectionControlsWorkInEveryFamilyAndIgnoreDisabledTouch() {
        PlatformFamily.entries.forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.CHECKBOX)
            onView(withId(R.id.sample_left)).perform(click()).check(matches(isChecked()))
            status("LEFT", "Checked")
            status("RIGHT", "Unchecked")
            compose.onNodeWithTag("enabled").performClick()
            onView(withId(R.id.sample_left)).perform(click()).check(matches(isChecked()))
            status("LEFT", "Checked")
            compose.onNodeWithTag("enabled").performClick()
            chooseComponent(LabComponent.SWITCH)
            onView(withId(R.id.sample_left)).perform(click()).check(matches(isChecked()))
            status("LEFT", "On")
            status("RIGHT", "Off")
            chooseComponent(LabComponent.RADIO)
            onView(allOf(withText("Option B"), isDescendantOfA(withId(R.id.sample_left))))
                .perform(click())
                .check(matches(isChecked()))
            status("LEFT", "Selected: Option B")
            status("RIGHT", "No selection")
        }
    }

    @Test
    fun textAndSlidersChangeOnlyTheTouchedPanelInEveryFamily() {
        PlatformFamily.entries.forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.TEXT_FIELD)
            onView(withId(R.id.sample_left))
                .perform(replaceText("Componentory"), closeSoftKeyboard())
            status("LEFT", "Text: Componentory")
            status("RIGHT", "Text: empty")
            chooseComponent(LabComponent.SLIDER)
            var touchedValue = 50
            dragSlider(0.5f, 0.9f)
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                touchedValue = (view as SeekBar).progress
                assertTrue(touchedValue > 50)
            }
            status("RIGHT", "Value: 50 / 100")
            compose.onNodeWithTag("enabled").performClick()
            dragSlider(0.9f, 0.5f)
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(touchedValue, (view as SeekBar).progress)
            }
            compose.onNodeWithTag("enabled").performClick()
            compose.onNodeWithTag("reset").performClick()
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(50, (view as SeekBar).progress)
            }
        }
    }

    @Test
    fun progressControlsChangeTheRealIndicatorAndRespectBounds() {
        PlatformFamily.entries.forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.PROGRESS)
            repeat(7) { compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick() }
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(100, (view as ProgressBar).progress)
            }
            status("RIGHT", "Value: 50 / 100")
            repeat(12) { compose.onNodeWithTag("decrease_LEFT").performClick() }
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(0, (view as ProgressBar).progress)
            }
        }
    }

    @Test
    fun dialogsReportConfirmCancelAndBackForEveryFamily() {
        chooseComponent(LabComponent.DIALOG)
        PlatformFamily.entries.forEach { family ->
            chooseFamily(family)
            onView(withId(R.id.sample_left)).perform(click())
            onView(withText("Confirm")).inRoot(isDialog()).perform(click())
            status("LEFT", "Last action: Confirmed")
            onView(withId(R.id.sample_left)).perform(click())
            onView(withText("Cancel")).inRoot(isDialog()).perform(click())
            status("LEFT", "Last action: Cancelled")
            onView(withId(R.id.sample_left)).perform(click())
            pressBack()
            status("LEFT", "Last action: Dismissed")
            status("RIGHT", "Last action: Not opened")
        }
    }

    @Test
    fun recreatingTheActivityRestoresTheActualWidgetState() {
        chooseFamily(PlatformFamily.MATERIAL)
        chooseComponent(LabComponent.CHECKBOX)
        onView(withId(R.id.sample_left)).perform(click())
        compose.activityRule.scenario.recreate()
        compose.waitForIdle()
        onView(withId(R.id.sample_left)).check(matches(isChecked()))
        status("LEFT", "Checked")
        status("RIGHT", "Unchecked")
        compose.onNodeWithTag("reset").performClick()
        onView(withId(R.id.sample_left)).check { view, error ->
            if (error != null) throw error
            assertTrue(!(view as CheckBox).isChecked)
        }
    }

    @Test
    fun compactWidthStacksPanelsAndKeepsBothSamplesReachable() {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme(dynamicColor = false) {
                    Box(Modifier.width(360.dp)) { LabScreen() }
                }
            }
        }
        compose.waitForIdle()
        val left = compose.onNodeWithTag("panel_LEFT").fetchSemanticsNode()
        val right = compose.onNodeWithTag("panel_RIGHT").fetchSemanticsNode()
        assertEquals(left.positionInRoot.x, right.positionInRoot.x, 1f)
        assertTrue(right.positionInRoot.y > left.positionInRoot.y + left.size.height)
        compose.onNodeWithTag("status_RIGHT").performScrollTo()
        onView(withId(R.id.sample_right)).perform(click())
        status("RIGHT", "Clicks: 1")
        status("LEFT", "Clicks: 0")
    }

    private fun dragSlider(fromFraction: Float, toFraction: Float) {
        var startX = 0f
        var endX = 0f
        var y = 0f
        compose.runOnIdle {
            val slider = compose.activity.findViewById<SeekBar>(R.id.sample_left)
            val position = IntArray(2)
            slider.getLocationOnScreen(position)
            startX = position[0] + slider.width * fromFraction
            endX = position[0] + slider.width * toFraction
            y = position[1] + slider.height / 2f
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val downTime = SystemClock.uptimeMillis()
        fun send(action: Int, x: Float) {
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                instrumentation.sendPointerSync(event)
            } finally {
                event.recycle()
            }
        }
        // Send the complete gesture before waiting for Compose; a held pointer is not idle.
        send(MotionEvent.ACTION_DOWN, startX)
        try {
            for (step in 1..12) {
                SystemClock.sleep(16)
                send(MotionEvent.ACTION_MOVE, startX + (endX - startX) * step / 12f)
            }
        } finally {
            send(MotionEvent.ACTION_UP, endX)
        }
        compose.waitForIdle()
    }

    private fun chooseFamily(family: PlatformFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_${component.name}").performScrollTo().performClick()
    }

    private fun status(panel: String, text: String) {
        compose.onNodeWithTag("status_$panel").assertTextEquals(text)
    }
}
