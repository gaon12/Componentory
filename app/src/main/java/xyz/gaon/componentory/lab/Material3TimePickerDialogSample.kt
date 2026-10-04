package xyz.gaon.componentory.lab

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun Material3TimePickerDialogSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    Button(
        onClick = {
            state.timeDraftMinutes = state.timeMinutes
            state.timeInputMode = false
            state.value = 1
        },
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(stringResource(R.string.open_time_picker))
    }
    if (state.value == 1) {
        val initial = SampleTimes.parts(state.timeDraftMinutes ?: state.timeMinutes)
        val pickerState =
            rememberTimePickerState(
                initialHour = initial.hour,
                initialMinute = initial.minute,
                is24Hour = state.time24Hour,
            )
        LaunchedEffect(pickerState) {
            snapshotFlow { SampleTimes.minutes(pickerState.hour, pickerState.minute) }
                .collect { state.timeDraftMinutes = it }
        }
        val canShowClock = timePickerClockFitsWindow()
        val displayMode =
            if (state.timeInputMode || !canShowClock) TimePickerDisplayMode.Input
            else TimePickerDisplayMode.Picker
        TimePickerDialog(
            onDismissRequest = {
                state.value = 4
                state.timeDraftMinutes = null
            },
            title = { TimePickerDialogDefaults.Title(displayMode) },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.timeMinutes =
                            SampleTimes.minutes(pickerState.hour, pickerState.minute)
                        state.timeDraftMinutes = null
                        state.value = 2
                    },
                    modifier = Modifier.testTag("time_confirm_$panel"),
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        state.value = 3
                        state.timeDraftMinutes = null
                    },
                    modifier = Modifier.testTag("time_cancel_$panel"),
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            modeToggleButton = {
                if (canShowClock) {
                    TimePickerDialogDefaults.DisplayModeToggle(
                        onDisplayModeChange = { state.timeInputMode = !state.timeInputMode },
                        displayMode = displayMode,
                        modifier = Modifier.testTag("time_mode_$panel"),
                    )
                }
            },
            modifier = Modifier.testTag("time_dialog_$panel"),
        ) {
            if (displayMode == TimePickerDisplayMode.Picker)
                TimePicker(pickerState, modifier = Modifier.testTag("time_picker_$panel"))
            else {
                TimeInput(pickerState, modifier = Modifier.testTag("time_input_$panel"))
            }
        }
    }
}

@Composable
internal fun timePickerClockFitsWindow(): Boolean {
    val height = LocalWindowInfo.current.containerSize.height
    val heightDp = with(LocalDensity.current) { height.toDp() }
    return heightDp > TimePickerDialogDefaults.MinHeightForTimePicker
}
