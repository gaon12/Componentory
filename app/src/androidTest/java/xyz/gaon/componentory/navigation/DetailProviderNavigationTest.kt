package xyz.gaon.componentory.navigation

import android.view.WindowManager
import android.widget.RatingBar
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

@RunWith(AndroidJUnit4::class)
class DetailProviderNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareCatalog() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.waitForIdle()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    @Test
    fun firstLibraryOnlyVisitsChooseMaterial2AndShowUsableSamples() {
        openComponent(LabComponent.RANGE_SLIDER)
        assertSupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL2)
        assertFeedback(LabComponent.RANGE_SLIDER, 20, rangeEnd = 80)

        returnToList()
        openComponent(LabComponent.BADGE)
        assertSupportedProvider(LabComponent.BADGE, DesignFamily.MATERIAL2)
        compose.onNodeWithTag("increase_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("badge_count_LEFT", useUnmergedTree = true).assertTextEquals("8")
        assertFeedback(LabComponent.BADGE, 8)
    }

    @Test
    fun firstMaterial3OnlyVisitThenPlatformOnlyVisitChooseTheirActualSuppliers() {
        openComponent(LabComponent.TONAL_BUTTON)
        assertSupportedProvider(LabComponent.TONAL_BUTTON, DesignFamily.MATERIAL3)
        compose.onNodeWithTag("library_LEFT").performScrollTo().performClick()
        assertFeedback(LabComponent.TONAL_BUTTON, 1)

        returnToList()
        openComponent(LabComponent.RATING)
        assertSupportedProvider(LabComponent.RATING, DesignFamily.CLASSIC)
        compose.onNodeWithTag("native_LEFT").performScrollTo()
        onView(withId(R.id.sample_left)).check(matches(isAssignableFrom(RatingBar::class.java)))
    }

    @Test
    fun deliberateUnsupportedChoiceIsRememberedAcrossOtherComponentsTabsAndRecreation() {
        openComponent(LabComponent.RANGE_SLIDER)
        chooseProvider(DesignFamily.CLASSIC)
        assertUnsupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)

        returnToList()
        openComponent(LabComponent.TONAL_BUTTON)
        assertSupportedProvider(LabComponent.TONAL_BUTTON, DesignFamily.MATERIAL3)
        returnToList()
        openComponent(LabComponent.RANGE_SLIDER)
        assertUnsupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)

        compose.onNodeWithTag("nav_settings").performClick()
        compose.onNodeWithTag("nav_list").performClick()
        assertUnsupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)
        compose.activityRule.scenario.recreate()
        assertUnsupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)

        // Revisit after recreation to verify the component map, not only the open detail state.
        returnToList()
        openComponent(LabComponent.TONAL_BUTTON)
        assertSupportedProvider(LabComponent.TONAL_BUTTON, DesignFamily.MATERIAL3)
        returnToList()
        openComponent(LabComponent.RANGE_SLIDER)
        assertUnsupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.CLASSIC)
        chooseProvider(DesignFamily.MATERIAL3)
        assertSupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL3)
        returnToList()
        openComponent(LabComponent.TONAL_BUTTON)
        returnToList()
        openComponent(LabComponent.RANGE_SLIDER)
        assertSupportedProvider(LabComponent.RANGE_SLIDER, DesignFamily.MATERIAL3)
    }

    @Test
    fun newComponentsKeepSupportedCurrentProviderAndAllManualChoicesRemainAvailable() {
        openComponent(LabComponent.BUTTON)
        chooseProvider(DesignFamily.MATERIAL3)
        returnToList()
        openComponent(LabComponent.CHECKBOX)
        assertSupportedProvider(LabComponent.CHECKBOX, DesignFamily.MATERIAL3)

        DesignFamily.entries.forEach { family ->
            chooseProvider(family)
            assertSupportedProvider(LabComponent.CHECKBOX, family)
        }
        chooseProvider(DesignFamily.HOLO)
        returnToList()
        openComponent(LabComponent.BUTTON)
        assertSupportedProvider(LabComponent.BUTTON, DesignFamily.MATERIAL3)
        returnToList()
        openComponent(LabComponent.SWITCH)
        assertSupportedProvider(LabComponent.SWITCH, DesignFamily.MATERIAL3)
    }

    private fun openComponent(component: LabComponent) {
        compose.onNodeWithTag("component_search").performTextReplacement(component.label)
        compose.onNodeWithTag("component_search").performImeAction()
        compose
            .onNodeWithTag("component_list")
            .performScrollToNode(hasTestTag("list_${component.name}"))
        compose.onNodeWithTag("list_${component.name}").performClick()
        compose.onNodeWithTag("detail_screen").assertExists()
    }

    private fun returnToList() {
        compose.onNodeWithTag("detail_back").performClick()
        compose.onNodeWithTag("list_screen").assertExists()
    }

    private fun chooseProvider(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performScrollTo().performClick()
    }

    private fun assertProviderMetadata(component: LabComponent, family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").assertTextEquals("${family.selectionLabel}  ▾")
        expandDetails("LEFT")
        compose
            .onNodeWithTag("source_LEFT")
            .assertTextEquals(family.source(component, compose.activity))
        val widgetApi =
            if (family.platform != null && component.platformSource != null)
                " · " + compose.activity.getString(R.string.widget_api, component.minimumApi)
            else ""
        expandDetails("LEFT")
        compose
            .onNodeWithTag("implementation_LEFT")
            .assertTextEquals(family.implementation(compose.activity) + widgetApi)
    }

    private fun assertSupportedProvider(component: LabComponent, family: DesignFamily) {
        assertProviderMetadata(component, family)
        compose.onNodeWithTag("unsupported_LEFT").assertDoesNotExist()
        if (family.platform != null) {
            compose.onNodeWithTag("native_LEFT").assertExists()
            compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
        } else {
            compose.onNodeWithTag("library_LEFT").assertExists()
            compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        }
    }

    private fun assertUnsupportedProvider(component: LabComponent, family: DesignFamily) {
        assertProviderMetadata(component, family)
        compose.onNodeWithTag("unsupported_LEFT").assertExists()
        compose.onNodeWithTag("native_LEFT").assertDoesNotExist()
        compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
    }

    private fun assertFeedback(component: LabComponent, value: Int, rangeEnd: Int = 80) {
        compose
            .onNodeWithTag("status_LEFT")
            .assertTextEquals(component.feedback(compose.activity, value, "", rangeEnd))
    }

    private fun expandDetails(panel: String) {
        val toggle = compose.onNodeWithTag("implementation_details_$panel").performScrollTo()
        if (
            toggle.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] !=
                ToggleableState.On
        )
            toggle.performTouchInput { click() }
    }
}
