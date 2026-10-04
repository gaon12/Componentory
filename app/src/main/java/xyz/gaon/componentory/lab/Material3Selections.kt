package xyz.gaon.componentory.lab

import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@Composable
internal fun Material3Selections(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val toggle = { state.value = if (state.value == 1) 0 else 1 }
    when (component) {
        LabComponent.ASSIST_CHIP ->
            AssistChip(
                onClick = { state.value++ },
                label = { Text(stringResource(R.string.sample_action)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.ELEVATED_ASSIST_CHIP ->
            ElevatedAssistChip(
                onClick = { state.value++ },
                label = { Text(stringResource(R.string.sample_action)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.FILTER_CHIP ->
            FilterChip(
                selected = state.value == 1,
                onClick = toggle,
                label = { Text(stringResource(R.string.sample_filter)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.ELEVATED_FILTER_CHIP ->
            ElevatedFilterChip(
                selected = state.value == 1,
                onClick = toggle,
                label = { Text(stringResource(R.string.sample_filter)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.INPUT_CHIP ->
            InputChip(
                selected = state.value == 1,
                onClick = toggle,
                label = { Text(stringResource(R.string.category_input)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.SUGGESTION_CHIP ->
            SuggestionChip(
                onClick = { state.value++ },
                label = { Text(stringResource(R.string.sample_suggestion)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.ELEVATED_SUGGESTION_CHIP ->
            ElevatedSuggestionChip(
                onClick = { state.value++ },
                label = { Text(stringResource(R.string.sample_suggestion)) },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.TRI_STATE_CHECKBOX ->
            TriStateCheckbox(
                state = state.triState,
                onClick = { state.value = (state.value + 1) % 3 },
                enabled = enabled,
                modifier = modifier,
            )
        LabComponent.SINGLE_SEGMENTED ->
            SingleChoiceSegmentedButtonRow(modifier) {
                for (index in 0..2) {
                    SegmentedButton(
                        selected = state.value == index + 1,
                        onClick = { state.value = index + 1 },
                        shape = SegmentedButtonDefaults.itemShape(index, 3),
                        enabled = enabled,
                        modifier = Modifier.testTag("library_${panel}_$index"),
                    ) {
                        Text(('A'.code + index).toChar().toString())
                    }
                }
            }
        LabComponent.MULTI_SEGMENTED ->
            MultiChoiceSegmentedButtonRow(modifier) {
                for (index in 0..2) {
                    val bit = 1 shl index
                    SegmentedButton(
                        checked = state.value and bit != 0,
                        onCheckedChange = { checked ->
                            state.value =
                                if (checked) state.value or bit else state.value and bit.inv()
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, 3),
                        enabled = enabled,
                        modifier = Modifier.testTag("library_${panel}_$index"),
                    ) {
                        Text(('A'.code + index).toChar().toString())
                    }
                }
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
