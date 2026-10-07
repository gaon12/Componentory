package xyz.gaon.componentory.lab

import android.app.ActionBar
import android.content.Intent
import android.content.res.Configuration
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
class ActionBarSampleTest {
    @Test
    fun holoAndMaterialActivitiesUseTheirRealFrameworkActionBars() {
        listOf(PlatformFamily.HOLO, PlatformFamily.MATERIAL).forEach { family ->
            launch(family).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue(ActionBar::class.java.isInstance(activity.actionBar))
                    assertEquals(
                        Configuration.UI_MODE_NIGHT_NO,
                        activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK,
                    )
                    assertEquals(family.themeName, activity.actionBar?.subtitle)
                }
                onView(withId(R.id.action_bar_sample_action)).perform(click())
                onView(withId(R.id.action_bar_sample_status)).check(matches(withText("Clicks: 1")))
                onView(withId(R.id.action_bar_sample_toggle)).perform(scrollTo(), click())
                scenario.onActivity { assertFalse(requireNotNull(it.actionBar).isShowing) }
                scenario.recreate()
                scenario.onActivity { assertFalse(requireNotNull(it.actionBar).isShowing) }
                onView(withId(R.id.action_bar_sample_status)).check(matches(withText("Clicks: 1")))
                onView(withId(R.id.action_bar_sample_toggle)).perform(scrollTo(), click())
                scenario.onActivity { assertTrue(requireNotNull(it.actionBar).isShowing) }
                onView(withId(R.id.action_bar_sample_reset)).perform(scrollTo(), click())
                onView(withId(R.id.action_bar_sample_status)).check(matches(withText("Clicks: 0")))
            }
        }
    }

    private fun launch(family: PlatformFamily): ActivityScenario<ActionBarSampleActivity> =
        ActivityScenario.launch(
            Intent(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    ActionBarSampleActivity::class.java,
                )
                .putExtra(ActionBarSampleActivity.EXTRA_FAMILY, family.name)
        )
}
