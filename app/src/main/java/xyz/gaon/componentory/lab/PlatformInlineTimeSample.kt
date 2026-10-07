package xyz.gaon.componentory.lab

import android.widget.TimePicker
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformInlineTimeSample(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    Box(
        Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("time_viewport_$panel")
    ) {
        AndroidView(
            factory = { context ->
                val initial = SampleTimes.parts(state.timeMinutes)
                TimePicker(family.createContext(context)).apply {
                    id = viewId
                    setIs24HourView(state.time24Hour)
                    hour = initial.hour
                    minute = initial.minute
                }
            },
            update = { picker ->
                picker.setOnTimeChangedListener(null)
                if (picker.is24HourView() != state.time24Hour) {
                    picker.setIs24HourView(state.time24Hour)
                }
                val selected = SampleTimes.parts(state.timeMinutes)
                if (picker.hour != selected.hour) picker.hour = selected.hour
                if (picker.minute != selected.minute) picker.minute = selected.minute
                picker.isEnabled = enabled
                picker.setOnTimeChangedListener { _, hour, minute ->
                    state.timeMinutes = SampleTimes.minutes(hour, minute)
                }
            },
            onReset = null,
            onRelease = { picker -> picker.setOnTimeChangedListener(null) },
            modifier = modifier.testTag("native_$panel"),
        )
    }
}
