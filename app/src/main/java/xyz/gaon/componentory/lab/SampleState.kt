package xyz.gaon.componentory.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState

@Composable
internal fun rememberSampleState(
    panel: String,
    family: DesignFamily,
    component: LabComponent,
    reset: Int,
): SampleState = rememberSampleStateSlot(panel, family, component, reset).value

@Composable
internal fun rememberSampleStateSlot(
    panel: String,
    family: DesignFamily,
    component: LabComponent,
    reset: Int,
    initialState: () -> SampleState = { SampleState(component.initialValue) },
): MutableState<SampleState> =
    // Equal providers must not let adjacent panels exchange their remembered state.
    key(panel, family, component, reset) {
        rememberSaveable(stateSaver = SampleState.Saver) { mutableStateOf(initialState()) }
    }

@Stable
class SampleState(
    initialValue: Int = 0,
    initialText: String = "",
    initialIcon: String = "",
    initialRangeEnd: Int = 80,
    initialDateUtcMillis: Long = SampleDates.INITIAL_UTC_MILLIS,
    initialDateDraftUtcMillis: Long? = null,
    initialTimeMinutes: Int = SampleTimes.INITIAL_MINUTES,
    initialTimeDraftMinutes: Int? = null,
    initialTime24Hour: Boolean = true,
    initialTimeInputMode: Boolean = false,
    initialContainerClickable: Boolean = true,
    initialInlineDateUtcMillis: Long? = SampleDates.INITIAL_UTC_MILLIS,
    initialDateRangeStartUtcMillis: Long? = null,
    initialDateRangeEndUtcMillis: Long? = null,
    initialDateInputMode: Boolean = false,
    initialDateDisplayedMonthUtcMillis: Long = SampleDates.INITIAL_MONTH_UTC_MILLIS,
    initialChronometerBaseMillis: Long = 0,
) {
    var value by mutableIntStateOf(initialValue)
    var text by mutableStateOf(initialText)
    var icon by mutableStateOf(initialIcon)
    var rangeEnd by mutableIntStateOf(initialRangeEnd)
    var dateUtcMillis by mutableLongStateOf(initialDateUtcMillis)
    var dateDraftUtcMillis by mutableStateOf(initialDateDraftUtcMillis)
    var timeMinutes by mutableIntStateOf(initialTimeMinutes)
    var timeDraftMinutes by mutableStateOf(initialTimeDraftMinutes)
    var time24Hour by mutableStateOf(initialTime24Hour)
    var timeInputMode by mutableStateOf(initialTimeInputMode)
    var containerClickable by mutableStateOf(initialContainerClickable)
    var inlineDateUtcMillis by mutableStateOf(initialInlineDateUtcMillis)
    var dateRangeStartUtcMillis by mutableStateOf(initialDateRangeStartUtcMillis)
    var dateRangeEndUtcMillis by mutableStateOf(initialDateRangeEndUtcMillis)
    var dateInputMode by mutableStateOf(initialDateInputMode)
    var dateDisplayedMonthUtcMillis by mutableLongStateOf(initialDateDisplayedMonthUtcMillis)
    // Running: wall-clock instant the counter started from zero. Stopped: frozen elapsed.
    var chronometerBaseMillis by mutableLongStateOf(initialChronometerBaseMillis)

    val triState: ToggleableState
        get() =
            when (value) {
                1 -> ToggleableState.On
                2 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

    companion object {
        val Saver = sampleStateSaver
    }
}
