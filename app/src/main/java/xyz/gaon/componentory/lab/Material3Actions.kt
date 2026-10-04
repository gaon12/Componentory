package xyz.gaon.componentory.lab

import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import xyz.gaon.componentory.R

@Composable
internal fun Material3Actions(
    component: LabComponent,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    when (component) {
        LabComponent.OUTLINED_BUTTON ->
            OutlinedButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                Text("Tap me")
            }
        LabComponent.TEXT_BUTTON ->
            TextButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                Text("Tap me")
            }
        LabComponent.ELEVATED_BUTTON ->
            ElevatedButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                Text("Tap me")
            }
        LabComponent.TONAL_BUTTON ->
            FilledTonalButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                Text("Tap me")
            }
        LabComponent.ICON_BUTTON ->
            IconButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                ActionIcon3()
            }
        LabComponent.ICON_TOGGLE ->
            IconToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3(state.value == 1)
            }
        LabComponent.FILLED_ICON_BUTTON ->
            FilledIconButton(onClick = { state.value++ }, enabled = enabled, modifier = modifier) {
                ActionIcon3()
            }
        LabComponent.FILLED_ICON_TOGGLE ->
            FilledIconToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3(state.value == 1)
            }
        LabComponent.TONAL_ICON_BUTTON ->
            FilledTonalIconButton(
                onClick = { state.value++ },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3()
            }
        LabComponent.TONAL_ICON_TOGGLE ->
            FilledTonalIconToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3(state.value == 1)
            }
        LabComponent.OUTLINED_ICON_BUTTON ->
            OutlinedIconButton(
                onClick = { state.value++ },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3()
            }
        LabComponent.OUTLINED_ICON_TOGGLE ->
            OutlinedIconToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = modifier,
            ) {
                ActionIcon3(state.value == 1)
            }
        LabComponent.FAB ->
            FloatingActionButton(onClick = { if (enabled) state.value++ }, modifier = modifier) {
                ActionIcon3()
            }
        LabComponent.EXTENDED_FAB ->
            ExtendedFloatingActionButton(
                text = { Text("Create") },
                icon = { ActionIcon3() },
                onClick = { if (enabled) state.value++ },
                modifier = modifier,
            )
        LabComponent.SMALL_FAB ->
            SmallFloatingActionButton(
                onClick = { if (enabled) state.value++ },
                modifier = modifier,
            ) {
                ActionIcon3()
            }
        LabComponent.LARGE_FAB ->
            LargeFloatingActionButton(
                onClick = { if (enabled) state.value++ },
                modifier = modifier,
            ) {
                ActionIcon3()
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}

@Composable
private fun ActionIcon3(selected: Boolean = false) {
    Icon(
        painterResource(if (selected) R.drawable.ic_close else R.drawable.ic_forward),
        contentDescription = if (selected) "Selected" else "Activate",
    )
}
