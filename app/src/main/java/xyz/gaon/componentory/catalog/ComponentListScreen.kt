package xyz.gaon.componentory.catalog

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.DesignFamily
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

// Rows answer "where can I use this?" without opening the detail screen, so
// the summary lists every provider that actually supports the component on
// this device's API level.
internal fun supportedFamilies(component: LabComponent, api: Int): List<DesignFamily> =
    DesignFamily.entries.filter { it.unsupportedReason(component, api) == null }

@Composable
private fun providerSummary(component: LabComponent): String {
    val supported = supportedFamilies(component, Build.VERSION.SDK_INT)
    return if (supported.size == DesignFamily.entries.size) stringResource(R.string.providers_all)
    else supported.joinToString(" · ") { it.label }
}

@Composable
fun ComponentListScreen(onOpenComponent: (LabComponent) -> Unit) {
    var mode by rememberSaveable { mutableStateOf(CatalogMode.SAMPLES) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    var provider by rememberSaveable { mutableStateOf<InventoryFamily?>(null) }
    val sampleListState = rememberLazyGridState()
    val plannedListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val changeQuery: (String) -> Unit = {
        query = it
        scope.launch {
            sampleListState.scrollToItem(0)
            plannedListState.scrollToItem(0)
        }
    }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                style = MaterialTheme.typography.bodyMedium,
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
        TextField(
            value = query,
            onValueChange = changeQuery,
            modifier =
                Modifier.fillMaxWidth().testTag("component_search").semantics {
                    contentDescription = searchLabel
                },
            placeholder = { Text(searchLabel, Modifier.clearAndSetSemantics {}) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { changeQuery("") },
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
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
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
            CategoryFilter(
                category,
                {
                    category = it
                    scope.launch { sampleListState.scrollToItem(0) }
                },
                "list",
                Modifier.fillMaxWidth(),
            )
            Text(
                pluralStringResource(R.plurals.component_count, components.size, components.size),
                modifier = Modifier.testTag("sample_count"),
                style = MaterialTheme.typography.labelLarge,
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(340.dp),
                state = sampleListState,
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("component_list"),
            ) {
                if (components.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
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
                                    changeQuery("")
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
                    Card(
                        onClick = {
                            focus.clearFocus()
                            onOpenComponent(component)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("list_${component.name}"),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                    ) {
                        ComponentSummary(component, providers = providerSummary(component)) {
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
