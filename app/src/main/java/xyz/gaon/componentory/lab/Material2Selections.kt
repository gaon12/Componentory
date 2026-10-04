// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2Selections(
    component: LabComponent,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    when (component) {
        LabComponent.CHIP ->
            Chip(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                Text(stringResource(R.string.sample_action))
            }
        LabComponent.FILTER_CHIP ->
            FilterChip(
                selected = state.value == 1,
                onClick = { state.value = if (state.value == 1) 0 else 1 },
                enabled = enabled,
                modifier = modifier,
            ) {
                Text(stringResource(R.string.sample_filter))
            }
        LabComponent.TRI_STATE_CHECKBOX ->
            TriStateCheckbox(
                state = state.triState,
                onClick = { state.value = (state.value + 1) % 3 },
                enabled = enabled,
                modifier = modifier,
            )
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
