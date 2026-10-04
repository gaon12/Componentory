package xyz.gaon.componentory.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState

@Composable
internal fun rememberSampleState(
    panel: String,
    family: DesignFamily,
    component: LabComponent,
    reset: Int,
): SampleState =
    // Equal providers must not let adjacent panels exchange their remembered state.
    key(panel, family, component, reset) {
        rememberSaveable(saver = SampleState.Saver) { SampleState(component.initialValue) }
    }

@Stable
class SampleState(
    initialValue: Int = 0,
    initialText: String = "",
    initialIcon: String = "",
    initialRangeEnd: Int = 80,
) {
    var value by mutableIntStateOf(initialValue)
    var text by mutableStateOf(initialText)
    var icon by mutableStateOf(initialIcon)
    var rangeEnd by mutableIntStateOf(initialRangeEnd)

    val triState: ToggleableState
        get() =
            when (value) {
                1 -> ToggleableState.On
                2 -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

    companion object {
        val Saver =
            listSaver<SampleState, Any>(
                save = { listOf(it.value, it.text, it.icon, it.rangeEnd) },
                restore = {
                    SampleState(
                        it[0] as Int,
                        it[1] as String,
                        it.getOrNull(2) as? String ?: "",
                        it.getOrNull(3) as? Int ?: 80,
                    )
                },
            )
    }
}
