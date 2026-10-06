package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

// BasicAlertDialog supplies only the window; the sample builds its own
// Surface content inside it, which is the documented purpose of the API.
// The outcome codes follow the shared dialog convention: opened, confirmed,
// cancelled and dismissed.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3BasicDialogSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    var open by remember { mutableStateOf(false) }
    Button(
        onClick = {
            open = true
            state.value = 1
        },
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(stringResource(R.string.open_dialog))
    }
    if (open) {
        BasicAlertDialog(
            onDismissRequest = {
                open = false
                state.value = 4
            },
            modifier = Modifier.testTag("library_${panel}_dialog"),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        stringResource(R.string.dialog_library_message, "Material 3"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Row(Modifier.align(Alignment.End).fillMaxWidth(), Arrangement.End) {
                        TextButton(
                            onClick = {
                                open = false
                                state.value = 3
                            },
                            modifier = Modifier.testTag("dialog_cancel"),
                        ) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                        TextButton(
                            onClick = {
                                open = false
                                state.value = 2
                            },
                            modifier = Modifier.testTag("dialog_confirm"),
                        ) {
                            Text(stringResource(R.string.dialog_confirm))
                        }
                    }
                }
            }
        }
    }
}
