// Samples use their actual library so the two design systems can be compared.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Material2Indicators(component: LabComponent, modifier: Modifier, state: SampleState) {
    when (component) {
        LabComponent.PROGRESS ->
            LinearProgressIndicator(
                progress = state.value / 100f,
                modifier = modifier.fillMaxWidth(),
            )
        LabComponent.CIRCULAR_PROGRESS ->
            CircularProgressIndicator(progress = state.value / 100f, modifier = modifier)
        LabComponent.INDETERMINATE_LINEAR_PROGRESS ->
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        LabComponent.INDETERMINATE_CIRCULAR_PROGRESS ->
            CircularProgressIndicator(modifier = modifier)
        else -> error("Unsupported Material 2 indicator: $component")
    }
}
