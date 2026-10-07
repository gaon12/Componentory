package xyz.gaon.componentory.lab

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.NinePatchDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import xyz.gaon.componentory.lab.recreation.ResourceToasts
import xyz.gaon.componentory.lab.recreation.ToastSample
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class ToastRecreationsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun allAvailableDesignsInflateTheirRecordedArtworkWithReadableTextAndRealPalettes() {
        availableFamilies().forEach { family ->
            show(family, SampleState())
            compose.onNodeWithTag("toast_preview_LEFT").assertIsDisplayed()
            compose.runOnIdle {
                val release = ResourceToasts.forFamily(family)
                val root = artwork(release.release)
                val text =
                    root.findViewById<TextView>(R.id.aosp_toast_text)
                        ?: requireNotNull(root.findViewById<TextView>(android.R.id.message))
                val minimum =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        16f,
                        root.resources.displayMetrics,
                    )
                assertEquals(minimum, text.textSize, 0.5f)
                assertEquals(
                    compose.activity.getString(R.string.toast_message),
                    text.text.toString(),
                )
                assertTrue(root.width > 0 && root.height > 0)
                val icon = root.findViewById<ImageView>(R.id.aosp_toast_icon)
                if (release.hasIcon) {
                    assertNotNull(icon.drawable)
                    assertEquals(2, text.maxLines)
                    assertEquals(
                        sampleColorScheme(family, compose.activity).surface.toArgb(),
                        (root.background as GradientDrawable).color!!.defaultColor,
                    )
                    assertEquals(
                        sampleColorScheme(family, compose.activity).onSurface.toArgb(),
                        text.currentTextColor,
                    )
                } else if (family != DesignFamily.MATERIAL2) {
                    assertTrue(root.background is NinePatchDrawable)
                    assertEquals(android.graphics.Color.WHITE, text.currentTextColor)
                } else {
                    assertEquals(
                        0xE6EEEEEE.toInt(),
                        (root.background as GradientDrawable).color!!.defaultColor,
                    )
                }
            }
        }
    }

    @Test
    fun retriggerRestartsThePopupLifetimeAndCountsEachSuccessfulOpen() {
        val state = SampleState()
        show(DesignFamily.MATERIAL3, state)
        tap(DesignFamily.MATERIAL3)
        compose.onNodeWithTag("toast_popup_LEFT").assertIsDisplayed()
        Thread.sleep(1200)
        tap(DesignFamily.MATERIAL3)
        Thread.sleep(1100)
        compose.onNodeWithTag("toast_popup_LEFT").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, state.value) }
        compose.waitUntil(10000) {
            compose.onAllNodesWithTag("toast_popup_LEFT").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithTag("toast_preview_LEFT").assertIsDisplayed()
    }

    @Test
    fun disablingAndDisposingTheSampleRemoveThePopupWithoutReopeningSavedCounts() {
        val state = SampleState(initialValue = 3)
        show(DesignFamily.CLASSIC, state)
        compose.onNodeWithTag("toast_popup_LEFT").assertDoesNotExist()
        tap(DesignFamily.CLASSIC)
        compose.onNodeWithTag("toast_popup_LEFT").assertIsDisplayed()
        show(DesignFamily.CLASSIC, state, enabled = false)
        compose.onNodeWithTag("toast_popup_LEFT").assertDoesNotExist()
        compose.runOnIdle {
            val button = compose.activity.findViewById<Button>(R.id.sample_left)
            assertFalse(button.isEnabled)
            button.performClick()
            assertEquals(4, state.value)
        }
        show(DesignFamily.CLASSIC, state)
        compose.onNodeWithTag("toast_popup_LEFT").assertDoesNotExist()
        tap(DesignFamily.CLASSIC)
        compose.onNodeWithTag("toast_popup_LEFT").assertIsDisplayed()
        show(DesignFamily.EXPRESSIVE, state)
        compose.onNodeWithTag("toast_popup_LEFT").assertDoesNotExist()
        tap(DesignFamily.EXPRESSIVE)
        compose.onNodeWithTag("toast_popup_LEFT").assertIsDisplayed()
        show(DesignFamily.EXPRESSIVE, state, enabled = false)
        compose.onNodeWithTag("library_LEFT").assertIsNotEnabled()
        compose.onNodeWithTag("toast_popup_LEFT").assertDoesNotExist()
        compose.runOnIdle { assertEquals(6, state.value) }
    }

    @Test
    fun modernToastTextAndIconFitANarrowViewportWithinTwoLines() {
        show(DesignFamily.MATERIAL3, SampleState(), width = 180)
        compose.runOnIdle {
            val root = artwork(ResourceToasts.forFamily(DesignFamily.MATERIAL3).release)
            val text = root.findViewById<TextView>(R.id.aosp_toast_text)
            val icon = root.findViewById<ImageView>(R.id.aosp_toast_icon)
            assertTrue(text.width > 0 && icon.width > 0)
            assertTrue(text.right <= root.width - root.paddingRight)
            assertTrue(text.layout.lineCount in 1..2)
            assertTrue(text.left >= icon.right)
        }
    }

    private fun availableFamilies() =
        DesignFamily.entries.filter {
            it.unsupportedReason(LabComponent.TOAST, Build.VERSION.SDK_INT) == null
        }

    private fun show(
        family: DesignFamily,
        state: SampleState,
        enabled: Boolean = true,
        width: Int = 320,
    ) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).width(width.dp)) {
                        key(family, state) { ToastSample(family, "LEFT", enabled, state) }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun tap(family: DesignFamily) {
        if (family.platform != null)
            compose.runOnIdle {
                compose.activity.findViewById<Button>(R.id.sample_left).performClick()
            }
        else compose.onNodeWithTag("library_LEFT").performClick()
        compose.waitForIdle()
    }

    private fun artwork(release: String): View {
        fun find(view: View): View? {
            if (view.getTag(R.id.aosp_resource_revision) == release) return view
            if (view is ViewGroup)
                for (index in 0 until view.childCount) find(view.getChildAt(index))?.let {
                    return it
                }
            return null
        }
        return requireNotNull(find(compose.activity.window.decorView))
    }
}
