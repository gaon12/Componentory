package xyz.gaon.componentory.lab.recreation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.Switch
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.PlatformFamily
import xyz.gaon.componentory.lab.PlatformSample
import xyz.gaon.componentory.lab.SamplePanel
import xyz.gaon.componentory.lab.SampleState
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class HistoricalControlsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun keepScreenOn() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    @Suppress("DEPRECATION")
    fun analogClocksUsePinnedArtworkAndFitTheirMeasuredViewport() {
        PlatformFamily.entries.forEach { family ->
            show(family, LabComponent.ANALOG_CLOCK, SampleState())
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertEquals(android.widget.AnalogClock::class.java, view.javaClass)
                assertEquals(
                    HistoricalControls.release(family),
                    view.getTag(R.id.aosp_resource_revision),
                )
                val visible = android.graphics.Rect()
                assertTrue(view.getGlobalVisibleRect(visible))
                assertTrue(view.width > 0 && view.height > 0)
                assertEquals(view.height, visible.height())
                val bitmap = draw(view)
                assertTrue(bitmap.width >= bitmap.height)
                assertTrue(view.isEnabled)
            }
        }
    }

    @Test
    fun releaseCheckboxGraphicsChangeWithTheLiveCheckedState() {
        PlatformFamily.entries.forEach { family ->
            val state = SampleState()
            show(family, LabComponent.CHECKBOX, state)
            lateinit var before: Bitmap
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                val checkbox = view as CheckBox
                assertEquals(
                    HistoricalControls.release(family),
                    view.getTag(R.id.aosp_resource_revision),
                )
                assertFalse(checkbox.isChecked)
                before = draw(checkbox)
            }
            onView(withId(R.id.sample_left)).perform(click())
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                assertTrue((view as CheckBox).isChecked)
                assertEquals(1, state.value)
                assertFalse(
                    "${family.name} checked artwork must visibly change",
                    before.sameAs(draw(view)),
                )
            }
        }
    }

    @Test
    fun switchesRadiosAndRatingsKeepWorkingWithReleaseDrawables() {
        PlatformFamily.entries.forEach { family ->
            val radioState = SampleState()
            show(family, LabComponent.RADIO, radioState)
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                (view as RadioGroup).getChildAt(1).performClick()
                assertEquals(2, radioState.value)
            }
            val ratingState = SampleState()
            show(family, LabComponent.RATING, ratingState)
            onView(withId(R.id.sample_left)).check { view, error ->
                if (error != null) throw error
                val rating = view as RatingBar
                assertEquals(5, rating.numStars)
                assertTrue(rating.width >= rating.progressDrawable.intrinsicWidth)
                assertTrue(rating.progressDrawable.findLayerByIdCompat())
                assertTrue(rating.width >= rating.height * 3)
            }
            if (family != PlatformFamily.CLASSIC) {
                val switchState = SampleState()
                show(family, LabComponent.SWITCH, switchState)
                onView(withId(R.id.sample_left)).perform(click())
                onView(withId(R.id.sample_left)).check { view, error ->
                    if (error != null) throw error
                    assertTrue((view as Switch).isChecked)
                    assertEquals(1, switchState.value)
                }
            }
        }
    }

    @Test
    fun unsupportedRecreationsRemainLabeledAsCurrentOsWidgets() {
        val state = SampleState()
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    SamplePanel(
                        "LEFT",
                        DesignFamily.CLASSIC,
                        {},
                        LabComponent.DATE_PICKER,
                        true,
                        0,
                        state,
                    )
                }
            }
        }
        compose.onNodeWithTag("rendering_LEFT").assertTextContains("Current-OS widget")
    }

    private fun show(family: PlatformFamily, component: LabComponent, state: SampleState) {
        compose.runOnUiThread {
            compose.activity.setContent {
                ComponentoryTheme {
                    Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        // Match SamplePanel's identity boundary when replacing native factories.
                        key(family, component, state) {
                            PlatformSample(
                                family,
                                component,
                                R.id.sample_left,
                                true,
                                state,
                                Modifier,
                            )
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun draw(view: View): Bitmap {
        view.jumpDrawablesToCurrentState()
        return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
            view.draw(Canvas(this))
        }
    }

    private fun android.graphics.drawable.Drawable.findLayerByIdCompat(): Boolean =
        (this as? android.graphics.drawable.LayerDrawable)?.findDrawableByLayerId(
            android.R.id.progress
        ) != null
}
