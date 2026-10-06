package xyz.gaon.componentory.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
    loading: Boolean = false,
    busy: Boolean = false,
    loadFailed: Boolean = false,
    onRetry: () -> Unit = {},
    onCreate: () -> Unit = {},
) {
    LazyColumn(
        Modifier.widthIn(max = 760.dp).fillMaxSize().testTag("runs_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "heading") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.nav_runs),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(
                    onClick = onExport,
                    enabled = records.isNotEmpty() && !busy && !loading,
                    modifier = Modifier.testTag("runs_export"),
                ) {
                    Text(stringResource(R.string.run_export))
                }
            }
        }
        when {
            loading || (busy && records.isEmpty()) ->
                item(key = "loading") {
                    Row(
                        Modifier.padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.runs_loading))
                    }
                }
            loadFailed ->
                item(key = "failure") {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                stringResource(R.string.run_load_failed),
                                Modifier.testTag("runs_load_failed"),
                            )
                            Button(
                                onClick = onRetry,
                                enabled = !busy,
                                modifier = Modifier.testTag("runs_retry"),
                            ) {
                                Text(stringResource(R.string.runs_retry))
                            }
                        }
                    }
                }
            records.isEmpty() ->
                item(key = "empty") {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Icon(
                                Icons.Outlined.History,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                stringResource(R.string.runs_empty),
                                Modifier.testTag("runs_empty"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Button(onClick = onCreate, modifier = Modifier.testTag("runs_create")) {
                                Text(stringResource(R.string.runs_create))
                            }
                        }
                    }
                }
        }
        items(records, key = { "record_${it.id}" }) { record ->
            RunRow(record, busy, onOpen, onDelete)
        }
    }
}

@Composable
private fun RunRow(
    record: RunRecord,
    busy: Boolean,
    onOpen: (RunRecord) -> Unit,
    onDelete: (RunRecord) -> Unit,
) {
    val component = LabComponent.entries.firstOrNull { it.name == record.component }
    val componentLabel = component?.let { stringResource(it.labelRes) } ?: record.component
    val timestamp =
        remember(record.createdAtEpochMillis) {
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(record.createdAtEpochMillis))
        }
    Card(
        Modifier.fillMaxWidth().testTag("run_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            // Keep provenance available without crowding the summary.
            if (record.environment.isNotEmpty()) {
                var expanded by rememberSaveable(record.id) { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(
                            value = expanded,
                            role = Role.Button,
                            onValueChange = { expanded = it },
                        )
                        .testTag("run_details_${record.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.run_environment),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (expanded) {
                    record.environment.toSortedMap().forEach { (key, value) ->
                        Text(
                            "$key: $value",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            // Rows repeat the same labels, so name the component for assistive tech.
            val openDescription = "${stringResource(R.string.run_open)} $componentLabel"
            val deleteDescription = "${stringResource(R.string.run_delete)} $componentLabel"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
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
                    enabled = !busy,
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
}

private fun familyLabel(name: String): String =
    DesignFamily.entries.firstOrNull { it.name == name }?.label ?: name
