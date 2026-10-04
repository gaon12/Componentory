package xyz.gaon.componentory.lab

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.provider.Settings
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.InputDevice
import android.view.MotionEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ProgressBar
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

@RunWith(AndroidJUnit4::class)
class NativeProgressIndicatorsTest {
    @get:Rule val activity = ActivityScenarioRule(MainActivity::class.java)
    private val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val originalFlags = automation.serviceInfo.flags
    private val originalAnimatorScale =
        Settings.Global.getString(
            InstrumentationRegistry.getInstrumentation().targetContext.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
        )

    @Before
    fun openComparison() {
        // Zero-duration native indeterminate animators can keep restarting on this device.
        shell("settings put global animator_duration_scale 1.0")
        automation.serviceInfo =
            automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            }
        activity.scenario.onActivity {
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        clickTag("nav_compare")
        clickTag("family_RIGHT")
        clickTag("family_RIGHT_MATERIAL3")
    }

    @After
    fun restoreAccessibilityFlags() {
        // Remove the live indicator before restoring a possibly zero animation duration.
        activity.scenario.close()
        require(originalAnimatorScale == null || originalAnimatorScale.toFloatOrNull() != null)
        shell(
            if (originalAnimatorScale == null) "settings delete global animator_duration_scale"
            else "settings put global animator_duration_scale $originalAnimatorScale"
        )
        automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use {
            it.readBytes()
        }
    }

    @Test
    fun nativeIndeterminateStylesUseLiveWidgetsWithWorkingLabControls() {
        // Native progress keeps drawing. Accessibility input does not require an idle renderer.
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            clickTag("family_LEFT")
            clickTag("family_LEFT_${family.name}")
            listOf(
                    LabComponent.INDETERMINATE_LINEAR_PROGRESS,
                    LabComponent.INDETERMINATE_CIRCULAR_PROGRESS,
                )
                .forEach { component ->
                    clickTag("component_picker")
                    val search =
                        waitForNode("picker search") { it.viewIdResourceName == "picker_search" }
                    assertTrue(
                        search.performAction(
                            AccessibilityNodeInfo.ACTION_SET_TEXT,
                            Bundle().apply {
                                putCharSequence(
                                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                    component.label,
                                )
                            },
                        )
                    )
                    clickTag("component_${component.name}")
                    waitForNode { it.viewIdResourceName == "xyz.gaon.componentory:id/sample_left" }
                    val bounds = Rect()
                    activity.scenario.onActivity {
                        val progress = it.findViewById<ProgressBar>(R.id.sample_left)
                        assertNotNull(progress)
                        assertEquals(ProgressBar::class.java, progress.javaClass)
                        assertTrue(progress.isShown)
                        assertTrue(progress.isIndeterminate)
                        assertNotNull(progress.indeterminateDrawable)
                        val expectedTheme = ContextThemeWrapper(it, family.platform!!.themeId).theme
                        listOf(
                                android.R.attr.progressBarStyle,
                                android.R.attr.progressBarStyleHorizontal,
                            )
                            .forEach { attribute ->
                                val expected = TypedValue()
                                val actual = TypedValue()
                                assertTrue(
                                    expectedTheme.resolveAttribute(attribute, expected, true)
                                )
                                assertTrue(
                                    progress.context.theme.resolveAttribute(attribute, actual, true)
                                )
                                assertEquals(expected.resourceId, actual.resourceId)
                            }
                        assertTrue(progress.getGlobalVisibleRect(bounds))
                    }
                    tap(bounds)
                    waitForNode { it.text?.toString() == "Progress: indeterminate" }
                    setEnabled(false)
                    activity.scenario.onActivity {
                        assertFalse(it.findViewById<ProgressBar>(R.id.sample_left).isEnabled)
                    }
                    setEnabled(true)
                    activity.scenario.onActivity {
                        assertTrue(it.findViewById<ProgressBar>(R.id.sample_left).isEnabled)
                    }
                }
        }
    }

    private fun clickTag(tag: String) {
        val node =
            waitForNode("tag: $tag") { it.viewIdResourceName == tag && clickableParent(it) != null }
        assertTrue(
            "Could not click $tag",
            requireNotNull(clickableParent(node)).performAction(AccessibilityNodeInfo.ACTION_CLICK),
        )
    }

    private fun clickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current = node
        while (current != null) {
            if (current.isClickable) return current
            current = current.parent
        }
        return null
    }

    // The boolean getter also works on the minimum supported Android API.
    @Suppress("DEPRECATION")
    private fun setEnabled(enabled: Boolean) {
        val control =
            waitForNode("Enabled switch") { it.viewIdResourceName == "enabled" && it.isCheckable }
        assertEquals(!enabled, control.isChecked)
        assertTrue(control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        waitForNode("Enabled switch state: $enabled") { it.isCheckable && it.isChecked == enabled }
    }

    private fun waitForNode(
        description: String = "expected node",
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            findNode(automation.rootInActiveWindow, predicate)?.let {
                return it
            }
            SystemClock.sleep(50)
        }
        error("The accessibility node did not appear: $description")
    }

    private fun findNode(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        if (predicate(node)) return node
        for (index in 0 until node.childCount) {
            findNode(node.getChild(index), predicate)?.let {
                return it
            }
        }
        return null
    }

    private fun tap(bounds: Rect) {
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event =
                MotionEvent.obtain(
                    time,
                    time + if (action == MotionEvent.ACTION_UP) 50 else 0,
                    action,
                    bounds.exactCenterX(),
                    bounds.exactCenterY(),
                    0,
                )
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            try {
                assertTrue(automation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
    }
}
