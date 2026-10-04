package xyz.gaon.componentory.lab

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inspector.WindowInspector
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29)
class PlatformInputsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun autocompletePopupsCompleteSingleAndCommaSeparatedInputsInEveryTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.AUTOCOMPLETE)
            clickNativeSample()
            onView(withId(R.id.sample_left)).perform(typeText("Al"), closeSoftKeyboard())
            status("LEFT", "Text: Al")
            waitForAutocompletePopup()
            choosePopupItem("Alpha")
            onView(withId(R.id.sample_left)).check(matches(withText("Alpha")))
            status("LEFT", "Text: Alpha")
            status("RIGHT", "Text: empty")
            chooseComponent(LabComponent.MULTI_AUTOCOMPLETE)
            clickNativeSample()
            onView(withId(R.id.sample_left)).perform(typeText("Alpha, Be"), closeSoftKeyboard())
            status("LEFT", "Text: Alpha, Be")
            waitForAutocompletePopup()
            choosePopupItem("Beta")
            onView(withId(R.id.sample_left)).check(matches(withText("Alpha, Beta, ")))
            status("LEFT", "Text: Alpha, Beta, ")
            compose.onNodeWithTag("enabled").performClick()
            onView(withId(R.id.sample_left)).check(matches(not(isEnabled())))
            compose.onNodeWithTag("enabled").performClick()
            compose.onNodeWithTag("reset").performClick()
            status("LEFT", "Text: empty")
        }
    }

    @Test
    fun spinnerSelectionUsesActualPopupItemsAndRestoresTheSelection() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.SPINNER)
            clickNativeSample()
            choosePopupItem("Beta")
            status("LEFT", "Selected: Beta")
            status("RIGHT", "Selected: Alpha")
            compose.activityRule.scenario.recreate()
            status("LEFT", "Selected: Beta")
            compose.onNodeWithTag("enabled").performClick()
            clickNativeSample()
            status("LEFT", "Selected: Beta")
            compose.onNodeWithTag("enabled").performClick()
            compose.onNodeWithTag("reset").performClick()
            status("LEFT", "Selected: Alpha")
        }
    }

    @Test
    fun searchViewSubmitsTheQueryAndDisablesItsEditableChildInEveryTheme() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            chooseFamily(family)
            chooseComponent(LabComponent.SEARCH_VIEW)
            val query =
                onView(
                    allOf(
                        isAssignableFrom(EditText::class.java),
                        isDescendantOfA(withId(R.id.sample_left)),
                    )
                )
            clickNativeSample()
            query.perform(typeText("Alpha"), pressImeActionButton(), closeSoftKeyboard())
            status("LEFT", "Query: Alpha · Searches: 1")
            status("RIGHT", "Query: empty · Searches: 0")
            compose.activityRule.scenario.recreate()
            status("LEFT", "Query: Alpha · Searches: 1")
            compose.onNodeWithTag("enabled").performClick()
            query.check(matches(not(isEnabled())))
            compose.onNodeWithTag("enabled").performClick()
            query.perform(replaceText(""), closeSoftKeyboard())
            status("LEFT", "Query: empty · Searches: 1")
            compose.onNodeWithTag("reset").performClick()
            status("LEFT", "Query: empty · Searches: 0")
        }
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
        compose.waitForIdle()
    }

    private fun clickNativeSample() {
        // Scroll and synchronize Compose before handing the native popup to Espresso.
        compose.runOnUiThread {
            // Inject hardware key events without moving the target under a software keyboard.
            suppressSoftwareKeyboard(compose.activity.findViewById(R.id.sample_left))
            val window = compose.activity.window
            WindowCompat.getInsetsController(window, window.decorView)
                .hide(WindowInsetsCompat.Type.ime())
        }
        waitForKeyboardToClose()
        compose.onNodeWithTag("native_LEFT").performScrollTo().performClick()
        compose.waitForIdle()
    }

    private fun suppressSoftwareKeyboard(view: View) {
        if (view is EditText) view.showSoftInputOnFocus = false
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) suppressSoftwareKeyboard(view.getChildAt(index))
        }
    }

    private fun waitForKeyboardToClose() {
        compose.waitUntil(5_000) {
            var hidden = false
            compose.runOnUiThread {
                hidden =
                    ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) != true
            }
            hidden
        }
        compose.waitForIdle()
    }

    private fun waitForAutocompletePopup() {
        compose.waitUntil(5_000) {
            var showing = false
            compose.runOnUiThread {
                showing =
                    compose.activity
                        .findViewById<AutoCompleteTextView>(R.id.sample_left)
                        .isPopupShowing
            }
            showing
        }
    }

    private fun choosePopupItem(value: String) {
        waitForKeyboardToClose()
        var item: View? = null
        compose.waitUntil(5_000) {
            compose.runOnUiThread {
                item =
                    WindowInspector.getGlobalWindowViews()
                        .filter { it != compose.activity.window.decorView }
                        .firstNotNullOfOrNull { findPopupText(it, value) }
            }
            item != null
        }
        val popupRoot = requireNotNull(item).rootView
        onView(withText(value))
            .inRoot(
                object : TypeSafeMatcher<Root>() {
                    override fun matchesSafely(root: Root) = root.decorView == popupRoot

                    override fun describeTo(description: Description) {
                        description.appendText("the native popup containing $value")
                    }
                }
            )
            .perform(click())
    }

    private fun findPopupText(view: View, value: String): View? {
        if (!view.isShown) return null
        if (view is TextView && view.text.toString() == value) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findPopupText(view.getChildAt(index), value)?.let {
                    return it
                }
            }
        }
        return null
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
        compose.waitForIdle()
    }

    private fun status(panel: String, text: String) {
        // Framework selection callbacks can finish after the popup touch returns.
        try {
            compose.waitUntil(5_000) {
                compose
                    .onAllNodes(hasTestTag("status_$panel") and hasText(text))
                    .fetchSemanticsNodes()
                    .size == 1
            }
        } catch (timeout: ComposeTimeoutException) {
            // Include the actual feedback in failures instead of only reporting a timeout.
            compose.onNodeWithTag("status_$panel").assertTextEquals(text)
            throw timeout
        }
        compose.onNodeWithTag("status_$panel").assertTextEquals(text)
    }
}
