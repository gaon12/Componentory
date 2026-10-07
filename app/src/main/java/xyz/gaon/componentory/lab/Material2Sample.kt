// Comparing both libraries is the purpose of this lab; samples keep separate themes and sources.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RangeSlider
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.roundToInt
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Material2Sample(component: LabComponent, panel: String, enabled: Boolean, state: SampleState) {
    var dialogOpen by remember { mutableStateOf(false) }
    val sample = Modifier.testTag("library_$panel")
    MaterialTheme(colors = lightColors()) {
        Surface(Modifier.fillMaxWidth()) {
            when (component) {
                LabComponent.TEXT ->
                    Text(stringResource(R.string.sample_display_text), modifier = sample)
                LabComponent.POPUP_MENU -> Material2PopupMenuSample(panel, sample, enabled, state)
                LabComponent.EXPOSED_DROPDOWN ->
                    Material2DropdownSample(component, panel, sample, enabled, state)
                LabComponent.BOTTOM_SHEET_SCAFFOLD,
                LabComponent.MODAL_BOTTOM_SHEET,
                LabComponent.BACKDROP_SCAFFOLD ->
                    Material2SheetSample(component, panel, sample, enabled, state)
                LabComponent.SWIPE_TO_DISMISS ->
                    Material2DismissSample(component, panel, sample, enabled, state)
                LabComponent.CARD,
                LabComponent.SURFACE -> Material2Containers(component, sample, enabled, state)
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
                        val label = stringResource(R.string.sample_checkbox)
                        Checkbox(
                            checked = state.value == 1,
                            onCheckedChange = { state.value = if (it) 1 else 0 },
                            enabled = enabled,
                            modifier = sample.semantics { contentDescription = label },
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.body1,
                            modifier =
                                Modifier.weight(1f)
                                    .testTag("sample_label_$panel")
                                    .clearAndSetSemantics {},
                        )
                    }
                LabComponent.SWITCH ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val label = stringResource(R.string.sample_switch)
                        Switch(
                            checked = state.value == 1,
                            onCheckedChange = { state.value = if (it) 1 else 0 },
                            enabled = enabled,
                            modifier = sample.semantics { contentDescription = label },
                        )
                        Text(
                            label,
                            style = MaterialTheme.typography.body1,
                            modifier =
                                Modifier.weight(1f)
                                    .testTag("sample_label_$panel")
                                    .clearAndSetSemantics {},
                        )
                    }
                LabComponent.RADIO ->
                    Column(Modifier.selectableGroup()) {
                        for (option in 1..2) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val label =
                                    stringResource(
                                        if (option == 1) R.string.option_a else R.string.option_b
                                    )
                                RadioButton(
                                    selected = state.value == option,
                                    onClick = { state.value = option },
                                    enabled = enabled,
                                    modifier =
                                        Modifier.testTag("library_${panel}_$option").semantics {
                                            contentDescription = label
                                        },
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.body1,
                                    modifier =
                                        Modifier.weight(1f)
                                            .testTag("sample_label_${panel}_$option")
                                            .clearAndSetSemantics {},
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
                            ComponentCategory.LAYOUT -> Material2Layouts(component, sample)
                            ComponentCategory.INDICATOR ->
                                Material2Indicators(component, panel, sample, state)
                            ComponentCategory.SELECTION ->
                                Material2Selections(component, sample, enabled, state)
                            ComponentCategory.INPUT ->
                                Material2Inputs(component, sample, enabled, state)
                            ComponentCategory.NAVIGATION ->
                                Material2Navigation(component, panel, sample, enabled, state)
                            ComponentCategory.FEEDBACK ->
                                Material2SnackbarSample(component, panel, sample, enabled, state)
                            else -> Material2Actions(component, sample, enabled, state)
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
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_library_message,
                            DesignFamily.MATERIAL2.label,
                        )
                    )
                },
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
