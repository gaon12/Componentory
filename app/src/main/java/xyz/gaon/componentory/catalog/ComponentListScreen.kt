package xyz.gaon.componentory.catalog

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.gaon.componentory.R
import xyz.gaon.componentory.eastereggs.easterEggReleases
import xyz.gaon.componentory.history.HistoryBrowser
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.DesignFamily
import xyz.gaon.componentory.lab.LabComponent

enum class CatalogMode(val labelRes: Int) {
    SAMPLES(R.string.catalog_mode_samples),
    // Retain the old enum name so older saved state can still be restored.
    PLANNED(R.string.catalog_mode_samples),
    HISTORY(R.string.catalog_mode_history);

    fun current(): CatalogMode = if (this == PLANNED) SAMPLES else this

    companion object {
        val visibleModes = listOf(SAMPLES, HISTORY)
    }
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
fun ComponentListScreen(
    mode: CatalogMode,
    onModeChange: (CatalogMode) -> Unit,
    selected: LabComponent? = null,
    onOpenComponent: (LabComponent) -> Unit,
    selectedEggId: String? = null,
    onOpenEgg: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    val sampleListState = rememberLazyGridState()
    val savedModes = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    val changeQuery: (String) -> Unit = {
        query = it
        scope.launch { sampleListState.scrollToItem(0) }
    }
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val components =
        LabComponent.entries.filter {
            it.matchesSearch(query, context) && (category == null || it.category == category)
        }
    val eggName = stringResource(R.string.category_easter_egg)
    val eggs =
        easterEggReleases.filter {
            it.matches(query, eggName) &&
                (category == null || category == ComponentCategory.EASTER_EGG)
        }
    val resultCount = components.size + eggs.size
    BoxWithConstraints(Modifier.widthIn(max = 900.dp).fillMaxSize()) {
        val compactHeader = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val shortWindow = maxHeight < 360.dp
        Column(
            Modifier.fillMaxSize().testTag("list_screen").padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (shortWindow) 8.dp else 12.dp),
        ) {
            if (!compactHeader && !shortWindow) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.components_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        stringResource(
                            when (mode) {
                                CatalogMode.SAMPLES,
                                CatalogMode.PLANNED -> R.string.catalog_intro
                                CatalogMode.HISTORY -> R.string.history_intro
                            }
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (!compactHeader) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    CatalogMode.visibleModes.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = mode.current() == option,
                            onClick = {
                                onModeChange(option)
                                focus.clearFocus()
                            },
                            shape =
                                SegmentedButtonDefaults.itemShape(
                                    index,
                                    CatalogMode.visibleModes.size,
                                ),
                            modifier = Modifier.testTag("catalog_mode_${option.name}"),
                            colors =
                                SegmentedButtonDefaults.colors(
                                    activeContainerColor =
                                        MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.primary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                    inactiveContentColor =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            border = BorderStroke(0.dp, Color.Transparent),
                            icon = {},
                        ) {
                            Text(stringResource(option.labelRes))
                        }
                    }
                }
            }
            val searchLabel =
                stringResource(
                    when (mode) {
                        CatalogMode.SAMPLES,
                        CatalogMode.PLANNED -> R.string.component_search_hint
                        CatalogMode.HISTORY -> R.string.history_search_hint
                    }
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
            if (mode == CatalogMode.HISTORY) {
                savedModes.SaveableStateProvider(CatalogMode.HISTORY.name) {
                    HistoryBrowser(query, Modifier.fillMaxWidth().weight(1f), onOpenComponent)
                }
            } else {
                if (!compactHeader)
                    CategoryFilter(
                        category,
                        {
                            category = it
                            scope.launch { sampleListState.scrollToItem(0) }
                        },
                        "list",
                        Modifier.fillMaxWidth(),
                        includeEasterEggs = true,
                    )
                Text(
                    pluralStringResource(R.plurals.component_count, resultCount, resultCount),
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
                    if (resultCount == 0) {
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
                            }
                        }
                    }
                    items(eggs, key = { "egg_${it.id}" }) { egg ->
                        Card(
                            onClick = {
                                focus.clearFocus()
                                onOpenEgg(egg.id)
                            },
                            modifier =
                                Modifier.fillMaxWidth().testTag("egg_${egg.id}").semantics {
                                    this.selected = selectedEggId == egg.id
                                },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (selectedEggId == egg.id)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.surface
                                ),
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    "$eggName · ${egg.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    egg.family.nickname,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    stringResource(R.string.egg_port_badge),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    items(components, key = { it.name }) { component ->
                        Card(
                            onClick = {
                                focus.clearFocus()
                                onOpenComponent(component)
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag("list_${component.name}")
                                    .semantics { this.selected = component == selected },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (component == selected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
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
}
