package xyz.gaon.componentory.lab

import android.view.WindowManager
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity

@RunWith(AndroidJUnit4::class)
class LibraryInputsTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun prepareComparison() {
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        compose.onNodeWithTag("nav_compare").performClick()
        chooseFamily("LEFT", DesignFamily.MATERIAL2)
        chooseFamily("RIGHT", DesignFamily.MATERIAL3)
    }

    @Test
    fun outlinedFieldsKeepTextIndependentAndRestoreItAfterRecreation() {
        chooseComponent(LabComponent.OUTLINED_TEXT_FIELD)
        compose.onNodeWithTag("library_LEFT").performTextInput("Alpha")
        closeSoftKeyboard()
        compose.onNodeWithTag("library_RIGHT").performTextInput("Beta")
        closeSoftKeyboard()
        compose.activityRule.scenario.recreate()
        status("LEFT", "Text: Alpha")
        status("RIGHT", "Text: Beta")
        listOf("LEFT", "RIGHT").forEach { panel ->
            val family = if (panel == "LEFT") DesignFamily.MATERIAL2 else DesignFamily.MATERIAL3
            compose
                .onNodeWithTag("source_$panel")
                .assertTextEquals(family.source(LabComponent.OUTLINED_TEXT_FIELD))
        }
        compose.onNodeWithTag("enabled").performClick()
        compose.onNodeWithTag("library_LEFT").assertIsNotEnabled().performTouchInput { click() }
        status("LEFT", "Text: Alpha")
        compose.onNodeWithTag("enabled").performClick()
        compose.onNodeWithTag("reset").performClick()
        status("LEFT", "Text: empty")
        status("RIGHT", "Text: empty")
    }

    @Test
    fun secureFieldsExposeOnlyLengthAndClearTheirContentsAfterRecreation() {
        listOf(LabComponent.SECURE_TEXT_FIELD, LabComponent.OUTLINED_SECURE_TEXT_FIELD).forEach {
            component ->
            chooseComponent(component)
            listOf("LEFT", "RIGHT").forEach { panel ->
                val field = compose.onNodeWithTag("library_$panel")
                field.performTextInput("sample123")
                closeSoftKeyboard()
                status(panel, "Characters: 9")
                assertEquals(Unit, field.fetchSemanticsNode().config[SemanticsProperties.Password])
            }
            compose.onNodeWithTag("enabled").performClick()
            compose.onNodeWithTag("library_LEFT").assertIsNotEnabled().performTouchInput { click() }
            status("LEFT", "Characters: 9")
            compose.onNodeWithTag("enabled").performClick()
            compose.activityRule.scenario.recreate()
            status("LEFT", "Characters: 0")
            status("RIGHT", "Characters: 0")
            compose.onNodeWithTag("library_LEFT").performTextInput("abc")
            closeSoftKeyboard()
            compose.onNodeWithTag("reset").performClick()
            status("LEFT", "Characters: 0")
            status("RIGHT", "Characters: 0")
        }
    }

    private fun chooseComponent(component: LabComponent) {
        compose.onNodeWithTag("component_picker").performScrollTo().performClick()
        compose.onNodeWithTag("picker_search").performTextReplacement(component.label)
        compose
            .onNodeWithTag("component_picker_list")
            .performScrollToNode(hasTestTag("component_${component.name}"))
        compose.onNodeWithTag("component_${component.name}").performClick()
    }

    private fun chooseFamily(panel: String, family: DesignFamily) {
        compose.onNodeWithTag("family_$panel").performClick()
        compose.onNodeWithTag("family_${panel}_${family.name}").performClick()
    }

    private fun status(panel: String, value: String) =
        compose.onNodeWithTag("status_$panel").assertTextEquals(value)
}
