// Both Material libraries are intentionally present as separate sample families.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import xyz.gaon.componentory.R

@Composable
internal fun Material2Actions(
    component: LabComponent,
    sample: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    when (component) {
        LabComponent.OUTLINED_BUTTON ->
            OutlinedButton(onClick = { state.value++ }, enabled = enabled, modifier = sample) {
                Text("Tap me")
            }
        LabComponent.TEXT_BUTTON ->
            TextButton(onClick = { state.value++ }, enabled = enabled, modifier = sample) {
                Text("Tap me")
            }
        LabComponent.ICON_BUTTON ->
            IconButton(onClick = { state.value++ }, enabled = enabled, modifier = sample) {
                ActionIcon2()
            }
        LabComponent.ICON_TOGGLE ->
            IconToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = sample,
            ) {
                ActionIcon2(state.value == 1)
            }
        LabComponent.FAB ->
            FloatingActionButton(onClick = { if (enabled) state.value++ }, modifier = sample) {
                ActionIcon2()
            }
        LabComponent.EXTENDED_FAB ->
            ExtendedFloatingActionButton(
                text = { Text("Create") },
                icon = { ActionIcon2() },
                onClick = { if (enabled) state.value++ },
                modifier = sample,
            )
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}

@Composable
private fun ActionIcon2(selected: Boolean = false) {
    Icon(
        painterResource(if (selected) R.drawable.ic_close else R.drawable.ic_forward),
        contentDescription = if (selected) "Selected" else "Activate",
    )
}
