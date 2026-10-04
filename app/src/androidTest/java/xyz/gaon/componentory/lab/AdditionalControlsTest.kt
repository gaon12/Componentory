package xyz.gaon.componentory.lab

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.RatingBar
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class AdditionalControlsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun nativeButtonsAndRatingAcceptTouchInEveryFrameworkTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.TOGGLE_BUTTON)
            onView(withId(R.id.sample_left)).perform(click())
            status("On")
            compose.onNodeWithTag("enabled").performClick()
            onView(withId(R.id.sample_left)).perform(click())
            status("On")
            compose.onNodeWithTag("enabled").performClick()
            compose.onNodeWithTag("reset").performClick()
            status("Off")
            chooseComponent(LabComponent.IMAGE_BUTTON)
            onView(withId(R.id.sample_left)).perform(click())
            status("Clicks: 1")
            chooseComponent(LabComponent.RATING)
            onView(withId(R.id.sample_left)).perform(click())
            compose.runOnUiThread {
                val rating = compose.activity.findViewById<RatingBar>(R.id.sample_left)
                assertTrue(rating.rating > 0)
                assertTrue(rating.numStars == 5)
            }
            compose.onNodeWithTag("status_RIGHT").assertTextEquals("Rating: 0 / 5")
        }
    }

    @Test
    fun numberPickerWheelChangesItsValueWithoutChangingTheOtherPanel() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.NUMBER_PICKER)
            if (family == DesignFamily.CLASSIC) {
                onView(withId(R.id.sample_left))
                    .perform(
                        GeneralClickAction(
                            Tap.SINGLE,
                            { picker ->
                                val button =
                                    descendants(picker).filterIsInstance<ImageButton>().first()
                                val position = IntArray(2)
                                button.getLocationOnScreen(position)
                                floatArrayOf(
                                    position[0] + button.width / 2f,
                                    position[1] + button.height / 2f,
                                )
                            },
                            Press.FINGER,
                            0,
                            0,
                        )
                    )
            } else onView(withId(R.id.sample_left)).perform(swipeUp())
            compose.runOnUiThread {
                assertTrue(compose.activity.findViewById<NumberPicker>(R.id.sample_left).value != 5)
            }
            compose.onNodeWithTag("status_RIGHT").assertTextEquals("Number: 5 / 10")
            compose.onNodeWithTag("reset").performClick()
            status("Number: 5 / 10")
        }
    }

    @Test
    fun unsupportedLibrariesShowAReasonAndNoInteractiveSubstitute() {
        listOf(
                LabComponent.TOGGLE_BUTTON,
                LabComponent.IMAGE_BUTTON,
                LabComponent.RATING,
                LabComponent.NUMBER_PICKER,
            )
            .forEach { component ->
                chooseComponent(component)
                listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
                    chooseFamily(family)
                    compose.onNodeWithTag("unsupported_LEFT").assertExists()
                    compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
                    compose.onNodeWithTag("status_LEFT").assertDoesNotExist()
                    onView(withId(R.id.sample_left)).check(doesNotExist())
                }
                chooseFamily(DesignFamily.CLASSIC)
                compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
            }
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }

    private fun status(text: String) {
        compose.onNodeWithTag("status_LEFT").assertTextEquals(text)
    }

    private fun descendants(view: View): List<View> =
        if (view is ViewGroup)
            (0 until view.childCount).flatMap { index ->
                listOf(view.getChildAt(index)) + descendants(view.getChildAt(index))
            }
        else emptyList()
}
