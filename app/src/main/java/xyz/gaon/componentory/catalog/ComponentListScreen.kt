package xyz.gaon.componentory.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

private enum class CatalogMode(val labelRes: Int) {
    SAMPLES(R.string.catalog_mode_samples),
    PLANNED(R.string.catalog_mode_planned),
}

private sealed interface InventoryLoadState {
    data object Loading : InventoryLoadState

    data object Unavailable : InventoryLoadState

    data class Ready(val entries: List<InventoryEntry>) : InventoryLoadState
}

@Composable
fun ComponentListScreen(onOpenComponent: (LabComponent) -> Unit) {
    var mode by rememberSaveable { mutableStateOf(CatalogMode.SAMPLES) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    var provider by rememberSaveable { mutableStateOf<InventoryFamily?>(null) }
    val sampleListState = rememberLazyListState()
    val plannedListState = rememberLazyListState()
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val inventory by
        produceState<InventoryLoadState>(InventoryLoadState.Loading, context) {
            value = InventoryLoadState.Loading
            value =
                withContext(Dispatchers.IO) {
                    try {
                        InventoryLoadState.Ready(ComponentInventory.read(context))
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        InventoryLoadState.Unavailable
                    }
                }
        }
    val entries = (inventory as? InventoryLoadState.Ready)?.entries
    val pendingMatches = entries?.let { ComponentInventory.pending(it, query) }
    val components =
        LabComponent.entries.filter {
            it.matchesSearch(query, context) && (category == null || it.category == category)
        }
    Column(
        Modifier.widthIn(max = 900.dp)
            .fillMaxSize()
            .testTag("list_screen")
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.components_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                stringResource(
                    if (mode == CatalogMode.SAMPLES) R.string.catalog_intro
                    else R.string.planned_intro
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            CatalogMode.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = mode == option,
                    onClick = {
                        mode = option
                        focus.clearFocus()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, CatalogMode.entries.size),
                    modifier = Modifier.testTag("catalog_mode_${option.name}"),
                ) {
                    Text(stringResource(option.labelRes))
                }
            }
        }
        val searchLabel =
            stringResource(
                if (mode == CatalogMode.SAMPLES) R.string.component_search_hint
                else R.string.planned_search_hint
            )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier =
                Modifier.fillMaxWidth().testTag("component_search").semantics {
                    contentDescription = searchLabel
                },
            label = { Text(searchLabel, Modifier.clearAndSetSemantics {}) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" },
                        modifier = Modifier.testTag("clear_search"),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.clear_search),
                        )
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        )
        if (mode == CatalogMode.PLANNED) {
            PlannedProviderFilter(provider, { provider = it }, Modifier.fillMaxWidth())
            when (val loaded = inventory) {
                InventoryLoadState.Loading,
                InventoryLoadState.Unavailable -> {
                    val loading = loaded == InventoryLoadState.Loading
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(
                                if (loading) R.string.planned_loading
                                else R.string.planned_unavailable
                            ),
                            modifier =
                                Modifier.testTag(
                                    if (loading) "planned_loading" else "planned_unavailable"
                                ),
                        )
                    }
                }
                is InventoryLoadState.Ready -> {
                    val planned = ComponentInventory.pending(loaded.entries, query, provider)
                    Text(
                        pluralStringResource(R.plurals.planned_count, planned.size, planned.size),
                        modifier = Modifier.testTag("planned_count"),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    PlannedApiList(planned, plannedListState, Modifier.fillMaxWidth().weight(1f))
                }
            }
        } else {
            CategoryFilter(category, { category = it }, "list", Modifier.fillMaxWidth())
            Text(
                pluralStringResource(R.plurals.component_count, components.size, components.size),
                modifier = Modifier.testTag("sample_count"),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyColumn(
                state = sampleListState,
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("component_list"),
            ) {
                if (components.isEmpty()) {
                    item {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.no_results),
                                modifier = Modifier.testTag("search_empty"),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TextButton(
                                onClick = {
                                    query = ""
                                    category = null
                                    focus.clearFocus()
                                },
                                modifier = Modifier.testTag("show_all_components"),
                            ) {
                                Text(stringResource(R.string.show_all_components))
                            }
                            if (query.isNotBlank() && !pendingMatches.isNullOrEmpty()) {
                                TextButton(
                                    onClick = {
                                        provider = null
                                        mode = CatalogMode.PLANNED
                                        focus.clearFocus()
                                    },
                                    modifier = Modifier.testTag("show_planned_matches"),
                                ) {
                                    Text(
                                        stringResource(
                                            R.string.planned_view_matches,
                                            pendingMatches.size,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                items(components, key = { it.name }) { component ->
                    OutlinedCard(
                        onClick = {
                            focus.clearFocus()
                            onOpenComponent(component)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("list_${component.name}"),
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    stringResource(component.labelRes),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    stringResource(component.descriptionRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                painterResource(R.drawable.ic_forward),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
