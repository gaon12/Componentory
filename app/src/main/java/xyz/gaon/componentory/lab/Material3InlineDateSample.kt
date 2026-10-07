package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun Material3InlineDateSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    state: SampleState,
) {
    val rangeHeightLimit = 520.dp * maxOf(1f, LocalDensity.current.fontScale)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Preserve the library's 360dp calendar width in a narrower host panel.
        val previewWidth = maxOf(maxWidth, 360.dp)
        SampleScrollViewport("date_viewport_$panel") {
            when (component) {
                LabComponent.DATE_PICKER -> {
                    val pickerState =
                        rememberDatePickerState(
                            initialSelectedDateMillis = state.inlineDateUtcMillis,
                            initialDisplayedMonthMillis = state.dateDisplayedMonthUtcMillis,
                            initialDisplayMode =
                                if (state.dateInputMode) DisplayMode.Input else DisplayMode.Picker,
                        )
                    LaunchedEffect(pickerState, state) {
                        snapshotFlow {
                                Triple(
                                    pickerState.selectedDateMillis,
                                    pickerState.displayMode,
                                    pickerState.displayedMonthMillis,
                                )
                            }
                            .collect { (date, mode, month) ->
                                state.inlineDateUtcMillis = date
                                state.dateInputMode = mode == DisplayMode.Input
                                state.dateDisplayedMonthUtcMillis = month
                            }
                    }
                    DatePicker(pickerState, modifier = modifier.width(previewWidth))
                }
                LabComponent.DATE_RANGE_PICKER -> {
                    val pickerState =
                        rememberDateRangePickerState(
                            initialSelectedStartDateMillis = state.dateRangeStartUtcMillis,
                            initialSelectedEndDateMillis = state.dateRangeEndUtcMillis,
                            initialDisplayedMonthMillis = state.dateDisplayedMonthUtcMillis,
                            initialDisplayMode =
                                if (state.dateInputMode) DisplayMode.Input else DisplayMode.Picker,
                        )
                    LaunchedEffect(pickerState, state) {
                        snapshotFlow {
                                DateRangeSnapshot(
                                    pickerState.selectedStartDateMillis,
                                    pickerState.selectedEndDateMillis,
                                    pickerState.displayMode,
                                    pickerState.displayedMonthMillis,
                                )
                            }
                            .collect { selection ->
                                state.dateRangeStartUtcMillis = selection.start
                                state.dateRangeEndUtcMillis = selection.end
                                state.dateInputMode = selection.mode == DisplayMode.Input
                                state.dateDisplayedMonthUtcMillis = selection.month
                            }
                    }
                    // Crossfade retains the month list in input mode. A font-scaled finite maximum
                    // bounds it while allowing the original input's natural height.
                    DateRangePicker(
                        pickerState,
                        modifier = modifier.width(previewWidth).heightIn(max = rangeHeightLimit),
                    )
                }
                else -> error("No Material 3 inline date sample for ${component.name}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private data class DateRangeSnapshot(
    val start: Long?,
    val end: Long?,
    val mode: DisplayMode,
    val month: Long,
)
