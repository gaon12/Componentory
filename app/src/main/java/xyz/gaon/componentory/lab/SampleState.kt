package xyz.gaon.componentory.lab

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

@Stable
class SampleState(initialValue: Int = 0, initialText: String = "") {
    var value by mutableIntStateOf(initialValue)
    var text by mutableStateOf(initialText)

    companion object {
        val Saver =
            listSaver<SampleState, Any>(
                save = { listOf(it.value, it.text) },
                restore = { SampleState(it[0] as Int, it[1] as String) },
            )
    }
}
