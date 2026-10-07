package xyz.gaon.componentory.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.gaon.componentory.R
import xyz.gaon.componentory.catalog.CategoryFilter
import xyz.gaon.componentory.catalog.ComponentSummary
import xyz.gaon.componentory.catalog.matchesSearch
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun ComponentPicker(component: LabComponent, onSelect: (LabComponent) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth().testTag("component_picker"),
        colors =
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Text(stringResource(component.labelRes), Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_forward), contentDescription = null)
    }
    if (!open) return
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    val context = LocalContext.current
    val options =
        LabComponent.entries.filter {
            it.matchesSearch(query, context) && (category == null || it.category == category)
        }
    val list = rememberLazyListState()
    LaunchedEffect(query, category) { list.scrollToItem(0) }
    val close = { open = false }
    Dialog(
        onDismissRequest = close,
        properties =
            DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val focus = LocalFocusManager.current
        BoxWithConstraints(
            Modifier.fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .semantics { testTagsAsResourceId = true },
            contentAlignment = Alignment.Center,
        ) {
            // Preserve a result viewport even when an IME or a short window limits height.
            val compactHeader =
                WindowInsets.ime.getBottom(LocalDensity.current) > 0 || maxHeight < 360.dp
            Card(Modifier.widthIn(max = 640.dp).fillMaxSize().testTag("component_picker_dialog")) {
                Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!compactHeader) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.compare_component_title),
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            TextButton(
                                onClick = close,
                                modifier = Modifier.testTag("picker_close"),
                            ) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                    // Keep the focused field in the same composition when the keyboard resizes the
                    // dialog.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        PickerSearch(
                            query,
                            { query = it },
                            { focus.clearFocus() },
                            Modifier.weight(1f),
                        )
                        if (compactHeader)
                            IconButton(
                                onClick = close,
                                modifier = Modifier.testTag("picker_close"),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_close),
                                    stringResource(R.string.close),
                                )
                            }
                    }
                    if (!compactHeader) {
                        CategoryFilter(
                            category,
                            { category = it },
                            "picker",
                            Modifier.fillMaxWidth(),
                        )
                    }
                    LazyColumn(
                        Modifier.fillMaxWidth()
                            .weight(1f)
                            .selectableGroup()
                            .testTag("component_picker_list"),
                        state = list,
                    ) {
                        if (options.isEmpty())
                            item {
                                Text(
                                    stringResource(R.string.no_results),
                                    Modifier.padding(16.dp).testTag("picker_empty"),
                                )
                            }
                        items(options, key = { it.name }) { option ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .selectable(
                                        selected = option == component,
                                        role = Role.RadioButton,
                                        onClick = {
                                            focus.clearFocus()
                                            onSelect(option)
                                            open = false
                                        },
                                    )
                                    .testTag("component_${option.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ComponentSummary(option, showDescription = false) {
                                    RadioButton(selected = option == component, onClick = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerSearch(
    query: String,
    onChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        singleLine = true,
        label = { Text(stringResource(R.string.component_search_hint)) },
        modifier = modifier.testTag("picker_search"),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onDone() }),
    )
}
