package xyz.gaon.componentory.lab

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun Material3InlineTimeSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    state: SampleState,
) {
    Box(
        Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("time_viewport_$panel")
    ) {
        // The library remembers initial arguments and its format flag is not observable state.
        // Rebuild the original control from the selected time when the host changes the format.
        key(state.time24Hour) {
            val initial = SampleTimes.parts(state.timeMinutes)
            val pickerState =
                rememberTimePickerState(
                    initialHour = initial.hour,
                    initialMinute = initial.minute,
                    is24Hour = state.time24Hour,
                )
            LaunchedEffect(pickerState, state) {
                snapshotFlow { SampleTimes.minutes(pickerState.hour, pickerState.minute) }
                    .collect { state.timeMinutes = it }
            }
            when (component) {
                LabComponent.TIME_PICKER -> TimePicker(pickerState, modifier = modifier)
                LabComponent.TIME_INPUT -> TimeInput(pickerState, modifier = modifier)
                else -> error("No Material 3 inline time sample for ${component.name}")
            }
        }
    }
}
