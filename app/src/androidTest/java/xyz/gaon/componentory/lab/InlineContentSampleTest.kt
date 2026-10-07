package xyz.gaon.componentory.lab

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import android.widget.EditText
import android.widget.inline.InlineContentView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.inline.InlineDemoActivity
import xyz.gaon.componentory.lab.inline.InlineDemoPhase
import xyz.gaon.componentory.lab.inline.InlineDemoSession

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 30)
class InlineContentSampleTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val keyboard = "xyz.gaon.componentory/.lab.inline.InlineDemoInputMethodService"

    @Test fun classicKeyboardHostsRealInlineContent() = verify(PlatformFamily.CLASSIC)

    @Test fun holoKeyboardHostsRealInlineContent() = verify(PlatformFamily.HOLO)

    @Test fun materialKeyboardHostsRealInlineContent() = verify(PlatformFamily.MATERIAL)

    private fun verify(family: PlatformFamily) {
        val context = instrumentation.targetContext
        // The dedicated host script records and restores both system roles in a finally block.
        assumeTrue(
            "Run scripts/test-inline-device.ps1 to enable the isolated demo services.",
            context
                .getSystemService(android.view.inputmethod.InputMethodManager::class.java)
                .enabledInputMethodList
                .any { it.id == keyboard } &&
                context
                    .getSystemService(android.view.autofill.AutofillManager::class.java)
                    .hasEnabledAutofillServices(),
        )
        instrumentation.uiAutomation.executeShellCommand("ime set $keyboard").use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        ActivityScenario.launch<InlineDemoActivity>(
                Intent(instrumentation.targetContext, InlineDemoActivity::class.java)
                    .putExtra(InlineDemoActivity.EXTRA_FAMILY, family.name)
            )
            .use { scenario ->
                scenario.onActivity { activity ->
                    assertEquals(
                        Configuration.UI_MODE_NIGHT_NO,
                        activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
                    )
                    activity.findViewById<EditText>(R.id.inline_sample_input).requestFocus()
                    activity.findViewById<View>(R.id.inline_sample_request).performClick()
                }
                waitFor(
                    "The platform did not attach an actual InlineContentView with a live surface."
                ) {
                    val view = find(R.id.inline_sample_content)
                    view is InlineContentView &&
                        view.isAttachedToWindow &&
                        view.width > 0 &&
                        view.height > 0 &&
                        view.surfaceControl?.isValid == true
                }
                var host: InlineContentView? = null
                var originalSurfaceOrder = false
                instrumentation.runOnMainSync {
                    host = find(R.id.inline_sample_content) as InlineContentView
                    originalSurfaceOrder = host!!.isZOrderedOnTop
                    assertEquals("android.widget.inline.InlineContentView", host!!.javaClass.name)
                    assertEquals(
                        Configuration.UI_MODE_NIGHT_NO,
                        host!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
                    )
                }
                instrumentation.sendStatus(
                    2,
                    Bundle().apply {
                        putString("inline_family", family.name)
                        putBoolean("inline_original_surface_on_top", originalSurfaceOrder)
                    },
                )
                touch(R.id.inline_sample_surface)
                instrumentation.runOnMainSync {
                    assertEquals(!originalSurfaceOrder, requireNotNull(host).isZOrderedOnTop)
                }
                touch(R.id.inline_sample_surface)
                instrumentation.runOnMainSync {
                    assertEquals(originalSurfaceOrder, requireNotNull(host).isZOrderedOnTop)
                }
                touch(R.id.inline_sample_attach)
                waitFor("The actual inline host did not detach.") {
                    host?.parent == null &&
                        InlineDemoSession.status.value.phase == InlineDemoPhase.DETACHED
                }
                touch(R.id.inline_sample_attach)
                waitFor("The actual inline host did not reattach.") {
                    host?.isAttachedToWindow == true &&
                        host?.surfaceControl?.isValid == true &&
                        InlineDemoSession.status.value.phase == InlineDemoPhase.ATTACHED
                }
                // Inject real system input. Do not inspect or dispatch events into the opaque
                // remote child.
                touch(R.id.inline_sample_content)
                waitFor("The real inline suggestion did not fill the fixed demo value.") {
                    (find(R.id.inline_sample_input) as? EditText)?.text?.toString() ==
                        InlineDemoSession.DEMO_VALUE
                }
            }
    }

    private fun waitFor(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 20_000
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            instrumentation.runOnMainSync { ready = condition() }
            if (!ready) SystemClock.sleep(100)
        }
        assertTrue(message, ready)
    }

    private fun find(id: Int): View? =
        WindowInspector.getGlobalWindowViews().firstNotNullOfOrNull { find(it, id) }

    private fun find(view: View, id: Int): View? {
        if (view.id == id) return view
        if (view is InlineContentView) return null
        if (view is ViewGroup)
            for (index in 0 until view.childCount) find(view.getChildAt(index), id)?.let {
                return it
            }
        return null
    }

    private fun touch(id: Int) {
        // The IME and remote renderer run outside this Activity's main-thread idle queue.
        instrumentation.uiAutomation.waitForIdle(300, 5_000)
        val location = IntArray(2)
        val visible = Rect()
        var x = 0f
        var y = 0f
        instrumentation.runOnMainSync {
            val view = requireNotNull(find(id)) { "Missing demo view $id" }
            assertTrue(view.isShown)
            assertTrue(view.getLocalVisibleRect(visible))
            view.getLocationOnScreen(location)
            x = location[0] + view.width / 2f
            y = location[1] + view.height / 2f
            assertTrue(
                "The real touch target must be visible.",
                visible.contains(view.width / 2, view.height / 2),
            )
        }
        val down = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, x, y, 0)
            try {
                assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true))
            } finally {
                event.recycle()
            }
        }
        instrumentation.waitForIdleSync()
    }
}
