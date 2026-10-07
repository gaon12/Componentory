package xyz.gaon.componentory.lab

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.gaon.componentory.MainActivity
import xyz.gaon.componentory.R
import xyz.gaon.componentory.ui.theme.ComponentoryTheme

@RunWith(AndroidJUnit4::class)
class SampleReadabilityTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun nativeLabelsKeepReadableSizesAcrossAllThreeThemes() {
        listOf(DesignFamily.CLASSIC, DesignFamily.HOLO, DesignFamily.MATERIAL).forEach { family ->
            listOf(
                    LabComponent.SWITCH,
                    LabComponent.CHECKBOX,
                    LabComponent.RADIO,
                    LabComponent.TEXT,
                    LabComponent.TEXT_FIELD,
                    LabComponent.NUMBER_PICKER,
                )
                .forEach { component ->
                    show(component, family)
                    compose.runOnIdle {
                        val labels = textViews(compose.activity.findViewById(R.id.sample_left))
                        assertTrue(
                            "${family.name} ${component.name} has visible text",
                            labels.isNotEmpty(),
                        )
                        labels.forEach(::assertReadable)
                    }
                }
        }
    }

    @Test
    fun adapterRowsKeepTheirTextSizeWhenTheSelectedItemChanges() {
        show(LabComponent.SPINNER, DesignFamily.HOLO)
        compose.runOnUiThread {
            compose.activity.findViewById<Spinner>(R.id.sample_left).setSelection(2)
        }
        compose.waitForIdle()
        compose.runOnIdle {
            val spinner = compose.activity.findViewById<Spinner>(R.id.sample_left)
            val selected = spinner.selectedView as TextView
            assertEquals("Gamma", selected.text.toString())
            assertReadable(selected)
        }
    }

    private fun show(component: LabComponent, family: DesignFamily) {
        val state = SampleState(component.initialValue)
        compose.runOnUiThread {
            compose.activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            compose.activity.setContent {
                ComponentoryTheme(darkTheme = false) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SamplePanel("LEFT", family, {}, component, true, 0, state)
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    private fun textViews(view: View): List<TextView> = buildList {
        if (view is TextView && view.visibility == View.VISIBLE) add(view)
        if (view is ViewGroup)
            for (index in 0 until view.childCount) {
                addAll(textViews(view.getChildAt(index)))
            }
    }

    private fun assertReadable(label: TextView) {
        val minimum =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                16f,
                label.resources.displayMetrics,
            )
        assertTrue(
            "${label.javaClass.simpleName}: ${label.textSize}px < $minimum",
            label.textSize >= minimum - 0.01f,
        )
    }
}
