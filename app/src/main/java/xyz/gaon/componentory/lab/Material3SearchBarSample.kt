package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import xyz.gaon.componentory.R

// The query text is the copied input; expansion state stays local because it
// is a transient window state, not an editable value.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3SearchBarSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val scope = rememberCoroutineScope()
    val inputField: @Composable (Boolean, (Boolean) -> Unit) -> Unit = { expanded, onExpanded ->
        SearchBarDefaults.InputField(
            query = state.text,
            onQueryChange = { state.text = it },
            onSearch = {},
            expanded = expanded,
            onExpandedChange = { if (enabled) onExpanded(it) },
            enabled = enabled,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            modifier = Modifier.testTag("library_${panel}_field"),
        )
    }
    val results: @Composable ColumnScope.() -> Unit = {
        for (item in 1..3) {
            val label = stringResource(R.string.list_item, item)
            TextButton(
                onClick = { state.text = label },
                enabled = enabled,
                modifier = Modifier.testTag("library_${panel}_result_$item"),
            ) {
                Text(label)
            }
        }
    }
    when (component) {
        LabComponent.SEARCH_BAR -> {
            var expanded by remember { mutableStateOf(false) }
            SearchBar(
                inputField = { inputField(expanded) { expanded = it } },
                expanded = expanded,
                onExpandedChange = { if (enabled) expanded = it },
                modifier = modifier,
                content = results,
            )
        }
        LabComponent.DOCKED_SEARCH_BAR -> {
            var expanded by remember { mutableStateOf(false) }
            DockedSearchBar(
                inputField = { inputField(expanded) { expanded = it } },
                expanded = expanded,
                onExpandedChange = { if (enabled) expanded = it },
                modifier = modifier,
                content = results,
            )
        }
        LabComponent.TOP_SEARCH_BAR -> {
            val barState = rememberSearchBarState()
            val topField: @Composable () -> Unit = {
                inputField(barState.targetValue == SearchBarValue.Expanded) { expanded ->
                    scope.launch {
                        if (expanded) barState.animateToExpanded()
                        else barState.animateToCollapsed()
                    }
                }
            }
            Column(modifier) {
                TopSearchBar(state = barState, inputField = { topField() })
                ExpandedFullScreenSearchBar(state = barState, inputField = { topField() }) {
                    results()
                }
            }
        }
        LabComponent.EXPANDED_DOCKED_SEARCH_BAR -> {
            val barState = rememberSearchBarState()
            val dockedField: @Composable () -> Unit = {
                inputField(barState.targetValue == SearchBarValue.Expanded) { expanded ->
                    scope.launch {
                        if (expanded) barState.animateToExpanded()
                        else barState.animateToCollapsed()
                    }
                }
            }
            Column(modifier) {
                TopSearchBar(state = barState, inputField = { dockedField() })
                ExpandedDockedSearchBar(state = barState, inputField = { dockedField() }) {
                    results()
                }
            }
        }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
