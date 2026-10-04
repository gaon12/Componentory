package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
fun Material3Sample(component: LabComponent, panel: String, enabled: Boolean, state: SampleState) {
    var dialogOpen by remember { mutableStateOf(false) }
    val sample = Modifier.testTag("library_$panel")
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(Modifier.fillMaxWidth()) {
            when (component) {
                LabComponent.ICON -> {
                    val icon = requireNotNull(LocalSampleIcon.current)
                    Icon(icon.vector(), contentDescription = icon.name, modifier = sample)
                }
                LabComponent.BUTTON ->
                    Button(onClick = { state.value++ }, enabled = enabled, modifier = sample) {
                        Text(stringResource(R.string.sample_button))
                    }
                LabComponent.CHECKBOX ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.value == 1,
                            onCheckedChange = { state.value = if (it) 1 else 0 },
                            enabled = enabled,
                            modifier = sample,
                        )
                        Text(stringResource(R.string.sample_checkbox))
                    }
                LabComponent.SWITCH ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = state.value == 1,
                            onCheckedChange = { state.value = if (it) 1 else 0 },
                            enabled = enabled,
                            modifier = sample,
                        )
                        Text(stringResource(R.string.sample_switch))
                    }
                LabComponent.RADIO ->
                    Column {
                        for (option in 1..2) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.value == option,
                                    onClick = { state.value = option },
                                    enabled = enabled,
                                    modifier = Modifier.testTag("library_${panel}_$option"),
                                )
                                Text(
                                    stringResource(
                                        if (option == 1) R.string.option_a else R.string.option_b
                                    )
                                )
                            }
                        }
                    }
                LabComponent.TEXT_FIELD ->
                    TextField(
                        value = state.text,
                        onValueChange = { state.text = it },
                        enabled = enabled,
                        singleLine = true,
                        label = { Text(stringResource(R.string.sample_hint)) },
                        modifier = sample,
                    )
                LabComponent.SLIDER ->
                    Slider(
                        value = state.value.toFloat(),
                        onValueChange = { state.value = it.roundToInt() },
                        valueRange = 0f..100f,
                        enabled = enabled,
                        modifier = sample,
                    )
                LabComponent.RANGE_SLIDER ->
                    RangeSlider(
                        value = state.value.toFloat()..state.rangeEnd.toFloat(),
                        onValueChange = {
                            state.value = it.start.roundToInt()
                            state.rangeEnd = it.endInclusive.roundToInt()
                        },
                        valueRange = 0f..100f,
                        enabled = enabled,
                        modifier = sample,
                    )
                LabComponent.DIALOG ->
                    Button(
                        onClick = {
                            dialogOpen = true
                            state.value = 1
                        },
                        enabled = enabled,
                        modifier = sample,
                    ) {
                        Text(stringResource(R.string.open_dialog))
                    }
                else ->
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth()) {
                        when (component.category) {
                            ComponentCategory.LAYOUT -> Material3Layouts(component, sample)
                            ComponentCategory.INDICATOR ->
                                Material3Indicators(component, sample, state)
                            ComponentCategory.SELECTION ->
                                Material3Selections(component, panel, sample, enabled, state)
                            ComponentCategory.INPUT ->
                                Material3Inputs(component, sample, enabled, state)
                            else -> Material3Actions(component, sample, enabled, state)
                        }
                    }
            }
        }
        if (dialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    dialogOpen = false
                    state.value = 4
                },
                title = { Text(stringResource(R.string.dialog_title)) },
                text = { Text(stringResource(R.string.dialog_library_message, "Material 3")) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialogOpen = false
                            state.value = 2
                        },
                        modifier = Modifier.testTag("dialog_confirm"),
                    ) {
                        Text(stringResource(R.string.dialog_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dialogOpen = false
                            state.value = 3
                        },
                        modifier = Modifier.testTag("dialog_cancel"),
                    ) {
                        Text(stringResource(R.string.dialog_cancel))
                    }
                },
            )
        }
    }
}
