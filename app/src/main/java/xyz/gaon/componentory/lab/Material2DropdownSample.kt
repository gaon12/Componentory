// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2DropdownSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    if (component != LabComponent.EXPOSED_DROPDOWN)
        error("Unsupported components must be handled by SamplePanel.")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = state.text,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.sample_hint)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.testTag("library_${panel}_field"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (item in 1..4) {
                val label = stringResource(R.string.list_item, item)
                DropdownMenuItem(
                    onClick = {
                        state.text = label
                        expanded = false
                    },
                    modifier = Modifier.testTag("library_${panel}_item_$item"),
                ) {
                    Text(label)
                }
            }
        }
    }
}
