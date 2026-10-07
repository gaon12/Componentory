package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExpressiveSample(
    component: LabComponent,
    panel: String,
    enabled: Boolean,
    state: SampleState,
) {
    val sample = Modifier.testTag("library_$panel")
    val action = stringResource(R.string.sample_button)
    val optionA = stringResource(R.string.option_a)
    val optionB = stringResource(R.string.option_b)
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) expanded = false }
    val icon = requireNotNull(LocalSampleIcon.current)
    val openMenu = stringResource(R.string.open_menu)
    when (component) {
        LabComponent.TOGGLE_BUTTON ->
            ToggleButton(
                checked = state.value == 1,
                onCheckedChange = { state.value = if (it) 1 else 0 },
                enabled = enabled,
                modifier = sample,
            ) {
                Text(stringResource(R.string.sample_switch))
            }
        LabComponent.BUTTON_GROUP ->
            ButtonGroup(
                overflowIndicator = { menu ->
                    TextButton(
                        onClick = { menu.show() },
                        enabled = enabled,
                        modifier =
                            Modifier.testTag("expressive_overflow_$panel").semantics {
                                contentDescription = openMenu
                            },
                    ) {
                        Text("…")
                    }
                },
                modifier = sample.fillMaxWidth(),
            ) {
                clickableItem(onClick = { state.value++ }, label = optionA, enabled = enabled)
                clickableItem(onClick = { state.value++ }, label = optionB, enabled = enabled)
            }
        LabComponent.SPLIT_BUTTON ->
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = { state.value++ },
                        enabled = enabled,
                        modifier = Modifier.testTag("expressive_primary_$panel"),
                    ) {
                        Text(action)
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(
                        onCheckedChange = { expanded = it },
                        enabled = enabled,
                        checked = expanded,
                        modifier = Modifier.testTag("expressive_secondary_$panel"),
                    ) {
                        Icon(icon.vector(), contentDescription = icon.name)
                    }
                },
                modifier = sample,
            )
        LabComponent.LOADING_INDICATOR,
        LabComponent.LINEAR_WAVY_PROGRESS,
        LabComponent.CIRCULAR_WAVY_PROGRESS ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val progress = { state.value.coerceIn(0, 100) / 100f }
                when (component) {
                    LabComponent.LOADING_INDICATOR ->
                        LoadingIndicator(progress = progress, modifier = sample)
                    LabComponent.LINEAR_WAVY_PROGRESS ->
                        LinearWavyProgressIndicator(
                            progress = progress,
                            modifier = sample.fillMaxWidth(),
                        )
                    else -> CircularWavyProgressIndicator(progress = progress, modifier = sample)
                }
            }
        LabComponent.HORIZONTAL_FLOATING_TOOLBAR,
        LabComponent.VERTICAL_FLOATING_TOOLBAR ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { expanded = !expanded },
                    enabled = enabled,
                    modifier = Modifier.testTag("expressive_expand_$panel"),
                ) {
                    Text(
                        stringResource(
                            if (expanded) R.string.expressive_collapse
                            else R.string.expressive_expand
                        )
                    )
                }
                if (component == LabComponent.HORIZONTAL_FLOATING_TOOLBAR)
                    HorizontalFloatingToolbar(
                        expanded = expanded,
                        modifier = sample,
                        leadingContent = { Text(optionA) },
                    ) {
                        IconButton(onClick = { state.value++ }, enabled = enabled) {
                            Icon(icon.vector(), icon.name)
                        }
                        IconButton(onClick = { state.value++ }, enabled = enabled) {
                            Icon(icon.vector(), icon.name)
                        }
                    }
                else
                    VerticalFloatingToolbar(
                        expanded = expanded,
                        modifier = sample,
                        leadingContent = { Text(optionA) },
                    ) {
                        IconButton(onClick = { state.value++ }, enabled = enabled) {
                            Icon(icon.vector(), icon.name)
                        }
                        IconButton(onClick = { state.value++ }, enabled = enabled) {
                            Icon(icon.vector(), icon.name)
                        }
                    }
            }
        LabComponent.FAB_MENU ->
            FloatingActionButtonMenu(
                expanded = expanded,
                button = {
                    FloatingActionButton(
                        onClick = { if (enabled) expanded = !expanded },
                        modifier =
                            Modifier.testTag("expressive_expand_$panel")
                                .alpha(if (enabled) 1f else 0.38f)
                                .semantics { if (!enabled) disabled() },
                    ) {
                        Icon(icon.vector(), icon.name)
                    }
                },
                modifier = sample,
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        if (enabled) {
                            state.value++
                            expanded = false
                        }
                    },
                    icon = { Icon(icon.vector(), icon.name) },
                    text = { Text(optionA) },
                )
                FloatingActionButtonMenuItem(
                    onClick = {
                        if (enabled) {
                            state.value++
                            expanded = false
                        }
                    },
                    icon = { Icon(icon.vector(), icon.name) },
                    text = { Text(optionB) },
                )
            }
        else -> error("No Expressive sample for $component")
    }
}
