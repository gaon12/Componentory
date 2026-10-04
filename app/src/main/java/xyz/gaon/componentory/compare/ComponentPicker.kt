package xyz.gaon.componentory.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.catalog.CategoryFilter
import xyz.gaon.componentory.lab.ComponentCategory
import xyz.gaon.componentory.lab.LabComponent

@Composable
fun ComponentPicker(component: LabComponent, onSelect: (LabComponent) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    OutlinedButton(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth().testTag("component_picker"),
    ) {
        Text(stringResource(component.labelRes))
    }
    if (open) {
        var query by rememberSaveable { mutableStateOf("") }
        var category by rememberSaveable { mutableStateOf<ComponentCategory?>(null) }
        val focus = LocalFocusManager.current
        val context = androidx.compose.ui.platform.LocalContext.current
        val options =
            LabComponent.entries.filter {
                it.matchesSearch(query, context) && (category == null || it.category == category)
            }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.compare_component_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.component_search_hint)) },
                        modifier = Modifier.fillMaxWidth().testTag("picker_search"),
                    )
                    CategoryFilter(category, { category = it }, "picker", Modifier.fillMaxWidth())
                    if (options.isEmpty())
                        Text(
                            stringResource(R.string.no_results),
                            modifier = Modifier.testTag("picker_empty"),
                        )
                    LazyColumn(
                        Modifier.heightIn(max = 420.dp)
                            .selectableGroup()
                            .testTag("component_picker_list")
                    ) {
                        items(options, key = { it.name }) { option ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .selectable(
                                        selected = option == component,
                                        role = Role.RadioButton,
                                        onClick = {
                                            focus.clearFocus()
                                            onSelect(option)
                                            open = false
                                        },
                                    )
                                    .testTag("component_${option.name}")
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = option == component, onClick = null)
                                Column {
                                    Text(
                                        stringResource(option.labelRes),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        stringResource(option.descriptionRes),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        focus.clearFocus()
                        open = false
                    },
                    modifier = Modifier.testTag("picker_close"),
                ) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}
