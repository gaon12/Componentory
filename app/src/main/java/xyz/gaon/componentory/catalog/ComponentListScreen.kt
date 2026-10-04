package xyz.gaon.componentory.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun ComponentListScreen(onOpenComponent: (LabComponent) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
    val focus = LocalFocusManager.current
    val context = androidx.compose.ui.platform.LocalContext.current
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
                stringResource(R.string.catalog_intro),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().testTag("component_search"),
            placeholder = { Text(stringResource(R.string.component_search_hint)) },
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
        CategoryFilter(category, { category = it }, "list", Modifier.fillMaxWidth())
        Text(
            pluralStringResource(R.plurals.component_count, components.size, components.size),
            style = MaterialTheme.typography.labelLarge,
        )
        LazyColumn(
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
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
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
