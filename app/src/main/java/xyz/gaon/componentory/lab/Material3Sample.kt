package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Shapes
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Typography
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.roundToInt
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

private val sampleTypography = Typography()
private val sampleShapes = Shapes()

@Composable
fun Material3Sample(component: LabComponent, panel: String, enabled: Boolean, state: SampleState) {
    var dialogOpen by remember { mutableStateOf(false) }
    val sample = Modifier.testTag("library_$panel")
    // App card corners must not change the pinned library's sample shapes.
    MaterialTheme(
        colorScheme = lightColorScheme(),
        typography = sampleTypography,
        shapes = sampleShapes,
    ) {
        Surface(Modifier.fillMaxWidth()) {
            when (component) {
                LabComponent.TEXT ->
                    Text(stringResource(R.string.sample_display_text), modifier = sample)
                LabComponent.DATE_PICKER,
                LabComponent.DATE_RANGE_PICKER ->
                    Material3InlineDateSample(component, panel, sample, state)
                LabComponent.TIME_PICKER,
                LabComponent.TIME_INPUT ->
                    Material3InlineTimeSample(component, panel, sample, state)
                LabComponent.POPUP_MENU -> Material3PopupMenuSample(panel, sample, enabled, state)
                LabComponent.EXPOSED_DROPDOWN ->
                    Material3DropdownSample(component, panel, sample, enabled, state)
                LabComponent.SEARCH_BAR,
                LabComponent.DOCKED_SEARCH_BAR,
                LabComponent.TOP_SEARCH_BAR,
                LabComponent.EXPANDED_DOCKED_SEARCH_BAR ->
                    Material3SearchBarSample(component, panel, sample, enabled, state)
                LabComponent.PULL_TO_REFRESH ->
                    Material3PullRefreshSample(panel, sample, enabled, state)
                LabComponent.MULTI_BROWSE_CAROUSEL,
                LabComponent.UNCONTAINED_CAROUSEL,
                LabComponent.CENTERED_HERO_CAROUSEL ->
                    Material3CarouselSample(component, panel, sample, enabled, state)
                LabComponent.BOTTOM_SHEET_SCAFFOLD,
                LabComponent.MODAL_BOTTOM_SHEET ->
                    Material3SheetSample(component, panel, sample, enabled, state)
                LabComponent.PLAIN_TOOLTIP,
                LabComponent.RICH_TOOLTIP,
                LabComponent.LABEL -> Material3TooltipSample(component, panel, sample, enabled)
                LabComponent.VERTICAL_DRAG_HANDLE ->
                    Material3DragHandleSample(panel, sample, enabled, state)
                LabComponent.SWIPE_TO_DISMISS ->
                    Material3DismissSample(component, panel, sample, enabled, state)
                LabComponent.CARD,
                LabComponent.ELEVATED_CARD,
                LabComponent.OUTLINED_CARD,
                LabComponent.SURFACE -> Material3Containers(component, sample, enabled, state)
                LabComponent.DATE_PICKER_DIALOG ->
                    Material3DatePickerDialogSample(panel, sample, enabled, state)
                LabComponent.TIME_PICKER_DIALOG ->
                    Material3TimePickerDialogSample(panel, sample, enabled, state)
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
                            style = MaterialTheme.typography.bodyLarge,
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
                            style = MaterialTheme.typography.bodyLarge,
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
                                    style = MaterialTheme.typography.bodyLarge,
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
                LabComponent.BASIC_ALERT_DIALOG ->
                    Material3BasicDialogSample(panel, sample, enabled, state)
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
                                Material3Indicators(component, panel, sample, state)
                            ComponentCategory.SELECTION ->
                                Material3Selections(component, panel, sample, enabled, state)
                            ComponentCategory.INPUT ->
                                Material3Inputs(component, sample, enabled, state)
                            ComponentCategory.NAVIGATION ->
                                Material3Navigation(component, panel, sample, enabled, state)
                            ComponentCategory.FEEDBACK ->
                                Material3SnackbarSample(component, panel, sample, enabled, state)
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
