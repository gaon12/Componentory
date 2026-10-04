package xyz.gaon.componentory.lab

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

@Composable
internal fun rememberSecureSampleState(sample: SampleState): TextFieldState {
    // Keep the demonstration input out of saved state and the visible feedback.
    val field = remember { TextFieldState() }
    LaunchedEffect(field, sample) {
        snapshotFlow { field.text.length }.collect { sample.value = it }
    }
    return field
}
