package xyz.gaon.componentory.lab

import android.view.View
import android.widget.TextClock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformClockSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    AndroidView(
        factory = { context ->
            createClock(family.createContext(context), component).apply { id = viewId }
        },
        update = { view ->
            view.isEnabled = enabled
            if (view is TextClock) {
                // Both fields are pinned so the sample's switch controls the display instead
                // of the system 12/24-hour preference.
                val pattern = if (state.time24Hour) "HH:mm:ss" else "h:mm:ss a"
                if (view.format24Hour.toString() != pattern) view.format24Hour = pattern
                if (view.format12Hour.toString() != pattern) view.format12Hour = pattern
            }
        },
        modifier = modifier.testTag("native_$panel"),
    )
}

// The deprecated clocks stay fully qualified so their imports cannot warn.
@Suppress("DEPRECATION")
private fun createClock(context: android.content.Context, component: LabComponent): View =
    when (component) {
        LabComponent.TEXT_CLOCK -> TextClock(context)
        LabComponent.ANALOG_CLOCK -> android.widget.AnalogClock(context)
        LabComponent.DIGITAL_CLOCK -> android.widget.DigitalClock(context)
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
