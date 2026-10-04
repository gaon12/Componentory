package xyz.gaon.componentory.lab

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState

@Stable
class SampleState(initialValue: Int = 0, initialText: String = "", initialIcon: String = "") {
    var value by mutableIntStateOf(initialValue)
    var text by mutableStateOf(initialText)
    var icon by mutableStateOf(initialIcon)

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
                save = { listOf(it.value, it.text, it.icon) },
                restore = {
                    SampleState(it[0] as Int, it[1] as String, it.getOrNull(2) as? String ?: "")
                },
            )
    }
}
