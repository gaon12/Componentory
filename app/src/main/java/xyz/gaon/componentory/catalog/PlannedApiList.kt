package xyz.gaon.componentory.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.BuildConfig
import xyz.gaon.componentory.R

@Composable
internal fun PlannedProviderFilter(
    selected: InventoryFamily?,
    onSelect: (InventoryFamily?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier.testTag("planned_providers"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.planned_provider_all)) },
                modifier = Modifier.testTag("planned_provider_ALL"),
            )
        }
        items(InventoryFamily.entries) { family ->
            FilterChip(
                selected = selected == family,
                onClick = { onSelect(family) },
                label = {
                    Text(
                        when (family) {
                            InventoryFamily.PLATFORM ->
                                stringResource(R.string.planned_provider_framework)
                            InventoryFamily.MATERIAL2 -> "Material 2"
                            InventoryFamily.MATERIAL3 -> "Material 3"
                        }
                    )
                },
                modifier = Modifier.testTag("planned_provider_${family.name}"),
            )
        }
    }
}

@Composable
internal fun PlannedApiList(
    entries: List<InventoryEntry>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.testTag("planned_list"),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (entries.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.planned_empty),
                    modifier =
                        Modifier.fillMaxWidth().padding(vertical = 32.dp).testTag("planned_empty"),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        items(entries, key = { "${it.family.name}_${it.source}" }) { entry ->
            val identity = "${entry.family.name}_${entry.source}"
            OutlinedCard(
                Modifier.fillMaxWidth().testTag("planned_$identity").semantics(
                    mergeDescendants = true
                ) {}
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        entry.source.substringAfterLast('.'),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        entry.source,
                        modifier = Modifier.testTag("source_$identity"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        when (entry.family) {
                            InventoryFamily.PLATFORM ->
                                stringResource(R.string.planned_provider_framework) +
                                    " · " +
                                    stringResource(
                                        R.string.planned_api_introduced,
                                        requireNotNull(entry.apiIntroduced),
                                    )
                            InventoryFamily.MATERIAL2 ->
                                "Compose Material 2 · ${BuildConfig.MATERIAL2_VERSION}"
                            InventoryFamily.MATERIAL3 ->
                                "Compose Material 3 · ${BuildConfig.MATERIAL3_VERSION}"
                        },
                        modifier = Modifier.testTag("provider_$identity"),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        stringResource(R.string.planned_status),
                        modifier = Modifier.testTag("status_$identity"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
