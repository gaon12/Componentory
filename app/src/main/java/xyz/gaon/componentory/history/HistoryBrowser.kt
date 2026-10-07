package xyz.gaon.componentory.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R

private sealed interface HistoryLoad {
    data object Loading : HistoryLoad

    data object Unavailable : HistoryLoad

    data class Ready(val catalog: AndroidHistory) : HistoryLoad
}

@Composable
fun HistoryBrowser(query: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val loaded by
        produceState<HistoryLoad>(HistoryLoad.Loading, context) {
            value =
                withContext(Dispatchers.IO) {
                    try {
                        HistoryLoad.Ready(AndroidHistory.read(context))
                    } catch (_: java.io.IOException) {
                        HistoryLoad.Unavailable
                    } catch (_: IllegalArgumentException) {
                        HistoryLoad.Unavailable
                    } catch (_: org.json.JSONException) {
                        HistoryLoad.Unavailable
                    }
                }
        }
    when (val state = loaded) {
        HistoryLoad.Loading ->
            Text(stringResource(R.string.history_loading), modifier.testTag("history_loading"))
        HistoryLoad.Unavailable ->
            Text(
                stringResource(R.string.history_unavailable),
                modifier.testTag("history_unavailable"),
            )
        is HistoryLoad.Ready -> HistoryList(state.catalog, query, modifier)
    }
}

@Composable
private fun HistoryList(catalog: AndroidHistory, query: String, modifier: Modifier) {
    var api by rememberSaveable { mutableIntStateOf(19) }
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    var menuOpen by remember { mutableStateOf(false) }
    val selectedApi = api.takeIf { it in catalog.versions } ?: catalog.versions.last()
    val entries = catalog.select(selectedApi, query, filter)
    val compactHeader = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!compactHeader) {
            Column {
                FilledTonalButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.fillMaxWidth().testTag("history_version"),
                ) {
                    Text(androidVersionLabel(selectedApi) + "  ▾")
                }
                DropdownMenu(
                    menuOpen,
                    { menuOpen = false },
                    Modifier.heightIn(max = 360.dp).semantics { testTagsAsResourceId = true },
                ) {
                    catalog.versions.reversed().forEach { version ->
                        DropdownMenuItem(
                            text = { Text(androidVersionLabel(version)) },
                            onClick = {
                                api = version
                                menuOpen = false
                            },
                            modifier = Modifier.testTag("history_api_$version"),
                        )
                    }
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HistoryFilter.entries) { option ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { filter = option },
                        label = {
                            Text(
                                stringResource(
                                    when (option) {
                                        HistoryFilter.ALL -> R.string.history_all
                                        HistoryFilter.ADDED -> R.string.history_added
                                        HistoryFilter.DEPRECATED -> R.string.history_deprecated
                                        HistoryFilter.REMOVED -> R.string.history_removed
                                    }
                                )
                            )
                        },
                        modifier = Modifier.testTag("history_filter_${option.name}"),
                        shape = MaterialTheme.shapes.small,
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                            ),
                        border = null,
                    )
                }
            }
        }
        Text(
            stringResource(R.string.history_count, entries.size),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("history_count"),
        )
        // Changing a version or filter begins a new list; returning to the tab retains it.
        androidx.compose.runtime.key(selectedApi, query, filter) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().testTag("history_list"),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!compactHeader)
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    stringResource(R.string.history_evidence),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("history_evidence"),
                                )
                                catalog.identicalSnapshots[selectedApi]?.let { other ->
                                    Text(
                                        stringResource(R.string.history_identical_snapshot, other),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Text(
                                    stringResource(
                                        R.string.history_source,
                                        catalog.sourceCommit.take(12),
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                if (entries.isEmpty())
                    item {
                        Text(
                            stringResource(R.string.no_results),
                            Modifier.padding(16.dp).testTag("history_empty"),
                        )
                    }
                items(entries, key = { it.name }) { entry ->
                    Card(Modifier.fillMaxWidth().testTag("history_${entry.name}")) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                entry.name.substringAfterLast('.'),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                entry.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                stringResource(
                                    R.string.history_since,
                                    catalog.introduced(entry.name),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (entry.abstract)
                                Text(
                                    stringResource(R.string.history_abstract),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            if (entry.deprecated)
                                Text(
                                    stringResource(R.string.history_deprecated_state),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            if (filter == HistoryFilter.REMOVED)
                                Text(
                                    stringResource(R.string.history_removed_at, selectedApi),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                        }
                    }
                }
            }
        }
    }
}
