package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import xyz.gaon.componentory.R

@Composable
internal fun ContainerSampleConfiguration(panel: String, enabled: Boolean, state: SampleState) {
    val label = stringResource(R.string.container_clickable)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = state.containerClickable,
                onCheckedChange = { state.containerClickable = it },
                enabled = enabled,
                modifier =
                    Modifier.testTag("container_clickable_$panel").semantics {
                        contentDescription = label
                    },
            )
            Text(label, modifier = Modifier.clearAndSetSemantics {})
        }
        Text(
            stringResource(
                if (state.containerClickable) R.string.container_clickable_overload
                else R.string.container_plain_overload
            ),
            modifier = Modifier.testTag("container_overload_$panel"),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            stringResource(R.string.container_enabled_note),
            modifier = Modifier.testTag("container_note_$panel"),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
