package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class LibraryActionsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val actions =
        LabComponent.entries.filter {
            it.platformSource == null && it.category == ComponentCategory.ACTION
        }

    @Before
    fun openComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
    }

    @Test
    fun everyAvailableActionRespondsToTouchAndRespectsDisabledState() {
        listOf(DesignFamily.MATERIAL2, DesignFamily.MATERIAL3).forEach { family ->
            chooseFamily(family)
            actions
                .filter { family.unsupportedReason(it, 36) == null }
                .forEach { component ->
                    chooseComponent(component)
                    compose.onNodeWithTag("source_LEFT").assertTextEquals(family.source(component))
                    val sample = compose.onNodeWithTag("library_LEFT")
                    sample.performScrollTo().performClick()
                    compose
                        .onNodeWithTag("status_LEFT")
                        .assertTextEquals(if (component.isIconToggle) "On" else "Clicks: 1")
                    if (component.isIconToggle) sample.assertIsOn()
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                    sample.performScrollTo().performTouchInput { click() }
                    compose
                        .onNodeWithTag("status_LEFT")
                        .assertTextEquals(if (component.isIconToggle) "On" else "Clicks: 1")
                    compose.onNodeWithTag("enabled").performScrollTo().performClick()
                    compose.onNodeWithTag("reset").performClick()
                    compose
                        .onNodeWithTag("status_LEFT")
                        .assertTextEquals(if (component.isIconToggle) "Off" else "Clicks: 0")
                    compose.onNodeWithTag("unsupported_RIGHT").assertExists()
                }
        }
    }

    @Test
    fun material3OnlyButtonsDoNotRenderInOlderFamilies() {
        chooseComponent(LabComponent.TONAL_BUTTON)
        listOf(
                DesignFamily.CLASSIC,
                DesignFamily.HOLO,
                DesignFamily.MATERIAL,
                DesignFamily.MATERIAL2,
            )
            .forEach { family ->
                chooseFamily(family)
                compose.onNodeWithTag("unsupported_LEFT").assertExists()
                compose.onNodeWithTag("library_LEFT").assertDoesNotExist()
            }
        chooseFamily(DesignFamily.MATERIAL3)
        compose.onNodeWithTag("library_LEFT").performClick()
        compose.onNodeWithTag("status_LEFT").assertTextEquals("Clicks: 1")
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(family: DesignFamily) {
        compose.onNodeWithTag("family_LEFT").performScrollTo().performClick()
        compose.onNodeWithTag("family_LEFT_${family.name}").performClick()
    }
}
