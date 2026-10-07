package xyz.gaon.componentory.lab

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.os.Build
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
            listOf(
                    LabComponent.INDETERMINATE_LINEAR_PROGRESS,
                    LabComponent.INDETERMINATE_CIRCULAR_PROGRESS,
                )
                .forEach { component ->
                    verifyCell(family, component) {
                        clickTag("family_LEFT")
                        clickTag("family_LEFT_${family.name}")
                        clickTag("component_picker")
                        clickTag("picker_category_ALL")
                        val search =
                            waitForNode("picker search") {
                                it.viewIdResourceName == "picker_search"
                            }
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
                        waitForTag("component_picker", "Selected component") {
                            findNode(it) { child -> child.text?.toString() == component.label } !=
                                null
                        }
                        if (!waitForTag("implementation_details_LEFT").isChecked) {
                            clickTag("implementation_details_LEFT")
                        }
                        waitForTag("source_LEFT", "Left framework source") {
                            it.text?.toString() == "android.widget.ProgressBar"
                        }
                        waitForTag("implementation_LEFT", "Left selected platform theme") {
                            it.text
                                ?.toString()
                                ?.startsWith("android:${family.platform!!.themeName}") == true
                        }
                        // Metadata scrolling must not turn an offscreen widget into tap evidence.
                        waitForTag(
                            "xyz.gaon.componentory:id/sample_left",
                            "Visible original indicator",
                        )
                        val bounds = Rect()
                        activity.scenario.onActivity {
                            val progress = it.findViewById<ProgressBar>(R.id.sample_left)
                            assertNotNull(progress)
                            assertEquals(ProgressBar::class.java, progress.javaClass)
                            assertEquals(
                                ContextThemeWrapper::class.java,
                                progress.context.javaClass,
                            )
                            assertTrue(progress.isShown)
                            assertTrue(progress.isIndeterminate)
                            assertNotNull(progress.indeterminateDrawable)
                            val expectedContext = ContextThemeWrapper(it, family.platform!!.themeId)
                            val expectedTheme = expectedContext.theme
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
                                        progress.context.theme.resolveAttribute(
                                            attribute,
                                            actual,
                                            true,
                                        )
                                    )
                                    assertEquals(expected.resourceId, actual.resourceId)
                                }
                            // A detached public constructor supplies a default-style reference.
                            // Drawable class and intrinsic size are limited constructor evidence,
                            // not pixel equality or evidence from a historical Android release.
                            val reference =
                                if (component == LabComponent.INDETERMINATE_LINEAR_PROGRESS)
                                    ProgressBar(
                                            expectedContext,
                                            null,
                                            android.R.attr.progressBarStyleHorizontal,
                                        )
                                        .apply { isIndeterminate = true }
                                else ProgressBar(expectedContext)
                            val expectedDrawable = requireNotNull(reference.indeterminateDrawable)
                            val actualDrawable = requireNotNull(progress.indeterminateDrawable)
                            assertEquals(expectedDrawable.javaClass, actualDrawable.javaClass)
                            assertEquals(
                                expectedDrawable.intrinsicWidth,
                                actualDrawable.intrinsicWidth,
                            )
                            assertEquals(
                                expectedDrawable.intrinsicHeight,
                                actualDrawable.intrinsicHeight,
                            )
                            assertTrue(progress.getGlobalVisibleRect(bounds))
                        }
                        tap(bounds)
                        waitForTag("status_LEFT", "Left indeterminate feedback") {
                            it.text?.toString() == "Progress: indeterminate"
                        }
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
    }

    private fun verifyCell(family: DesignFamily, component: LabComponent, verify: () -> Unit) {
        val cell =
            "${component.name}/${family.name} · android.widget.ProgressBar · ${family.platform!!.themeName} · API ${Build.VERSION.SDK_INT} · ${Build.DISPLAY}"
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.sendStatus(
            0,
            Bundle().apply { putString("stream", "\nNative animated cell BEGIN: $cell\n") },
        )
        try {
            verify()
        } catch (failure: Throwable) {
            throw AssertionError("Native animated catalog cell failed: $cell", failure)
        }
        instrumentation.sendStatus(
            0,
            Bundle().apply { putString("stream", "\nNative animated cell PASS: $cell\n") },
        )
    }

    private fun clickTag(tag: String) {
        // Accessibility nodes found while a dialog list is still rebuilding can go stale
        // before the click lands, so refresh the resolved node and retry briefly.
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (true) {
            val node = waitForTag(tag) { clickableParent(it) != null }
            val target = requireNotNull(clickableParent(node))
            if (target.refresh() && target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return
            }
            if (SystemClock.uptimeMillis() >= deadline) break
            SystemClock.sleep(150)
        }
        error("Could not click $tag")
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
        val control = waitForTag("enabled", "Enabled switch") { it.isCheckable }
        assertEquals(!enabled, control.isChecked)
        assertTrue(control.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        waitForNode("Enabled switch state: $enabled") {
            it.viewIdResourceName == "enabled" && it.isCheckable && it.isChecked == enabled
        }
    }

    private fun waitForTag(
        tag: String,
        description: String = tag,
        predicate: (AccessibilityNodeInfo) -> Boolean = { true },
    ): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 10_000
        var action = AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        var searchedToEnd = false
        while (SystemClock.uptimeMillis() < deadline) {
            val root = automation.rootInActiveWindow
            findNode(root) { it.viewIdResourceName == tag && it.isVisibleToUser && predicate(it) }
                ?.let {
                    return it
                }
            if (!searchedToEnd) {
                val host =
                    findNode(root) { it.viewIdResourceName == "compare_screen" && it.isScrollable }
                if (host != null && !host.performAction(action)) {
                    if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
                        action = AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    } else searchedToEnd = true
                }
            }
            // Public accessibility scrolling can update across frames while the widget draws.
            SystemClock.sleep(150)
        }
        error("The visible accessibility tag did not appear: $description ($tag)")
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
