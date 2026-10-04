package xyz.gaon.componentory.lab

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun Material3DatePickerDialogSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    Button(
        onClick = {
            state.dateDraftUtcMillis = state.dateUtcMillis
            state.value = 1
        },
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(stringResource(R.string.open_date_picker))
    }
    if (state.value == 1) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = state.dateDraftUtcMillis ?: state.dateUtcMillis
            )
        LaunchedEffect(pickerState) {
            snapshotFlow { pickerState.selectedDateMillis }
                .collect { state.dateDraftUtcMillis = it }
        }
        DatePickerDialog(
            onDismissRequest = {
                state.value = 4
                state.dateDraftUtcMillis = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { state.dateUtcMillis = it }
                        state.value = 2
                        state.dateDraftUtcMillis = null
                    },
                    enabled = pickerState.selectedDateMillis != null,
                    modifier = Modifier.testTag("date_confirm_$panel"),
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        state.value = 3
                        state.dateDraftUtcMillis = null
                    },
                    modifier = Modifier.testTag("date_cancel_$panel"),
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            modifier = Modifier.testTag("date_dialog_$panel"),
        ) {
            // Scroll the original content while the library keeps its action buttons visible.
            DatePicker(
                pickerState,
                modifier =
                    Modifier.verticalScroll(rememberScrollState()).testTag("date_picker_$panel"),
            )
        }
    }
}
