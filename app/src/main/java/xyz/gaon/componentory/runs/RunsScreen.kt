package xyz.gaon.componentory.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun RunsScreen(
    records: List<RunRecord>,
    onOpen: (RunRecord) -> Unit,
    onDelete: (RunRecord) -> Unit,
    onExport: () -> Unit,
) {
    Column(
        Modifier.widthIn(max = 760.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("runs_screen")
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.nav_runs),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
            )
            TextButton(
                onClick = onExport,
                enabled = records.isNotEmpty(),
                modifier = Modifier.testTag("runs_export"),
            ) {
                Text(stringResource(R.string.run_export))
            }
        }
        if (records.isEmpty()) {
            Text(
                stringResource(R.string.runs_empty),
                modifier = Modifier.testTag("runs_empty"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        records.forEach { record -> RunRow(record, onOpen, onDelete) }
    }
}

@Composable
private fun RunRow(record: RunRecord, onOpen: (RunRecord) -> Unit, onDelete: (RunRecord) -> Unit) {
    val component = LabComponent.entries.firstOrNull { it.name == record.component }
    val componentLabel = component?.let { stringResource(it.labelRes) } ?: record.component
    val timestamp =
        remember(record.createdAtEpochMillis) {
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(record.createdAtEpochMillis))
        }
    Column(Modifier.fillMaxWidth().testTag("run_${record.id}")) {
        Text(componentLabel, style = MaterialTheme.typography.titleMedium)
        Text(
            "${stringResource(R.string.left_ui)}: ${familyLabel(record.leftFamily)} · " +
                "${stringResource(R.string.right_ui)}: ${familyLabel(record.rightFamily)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(timestamp, style = MaterialTheme.typography.bodySmall)
        // A run is evidence; show the device and OS it actually ran on.
        listOfNotNull(
                record.environment["model"],
                record.environment["androidRelease"]?.let { "Android $it" },
                record.environment["api"]?.let { "API $it" },
            )
            .takeIf { it.isNotEmpty() }
            ?.let {
                Text(
                    it.joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        // Rows repeat the same labels, so name the component for assistive tech.
        val openDescription = "${stringResource(R.string.run_open)} $componentLabel"
        val deleteDescription = "${stringResource(R.string.run_delete)} $componentLabel"
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { onOpen(record) },
                modifier =
                    Modifier.testTag("run_open_${record.id}").semantics {
                        contentDescription = openDescription
                    },
            ) {
                Text(stringResource(R.string.run_open))
            }
            TextButton(
                onClick = { onDelete(record) },
                modifier =
                    Modifier.testTag("run_delete_${record.id}").semantics {
                        contentDescription = deleteDescription
                    },
            ) {
                Text(stringResource(R.string.run_delete))
            }
        }
    }
}

private fun familyLabel(name: String): String =
    DesignFamily.entries.firstOrNull { it.name == name }?.label ?: name
