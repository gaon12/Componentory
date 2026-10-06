package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider as LegacyDivider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

// Keep the deprecated function available as its own source sample.
@Suppress("DEPRECATION")
@Composable
internal fun Material3Layouts(component: LabComponent, modifier: Modifier) {
    when (component) {
        LabComponent.HORIZONTAL_DIVIDER -> HorizontalDivider(modifier = modifier)
        LabComponent.VERTICAL_DIVIDER -> VerticalDivider(modifier = modifier.height(64.dp))
        LabComponent.LEGACY_DIVIDER -> LegacyDivider(modifier = modifier)
        LabComponent.LIST_ITEM ->
            ListItem(
                headlineContent = { Text(stringResource(R.string.list_item, 1)) },
                supportingContent = { Text(stringResource(R.string.component_list_item)) },
                modifier = modifier,
            )
        LabComponent.SCAFFOLD ->
            Scaffold(modifier = modifier) { padding ->
                Text(stringResource(R.string.scaffold_body), modifier = Modifier.padding(padding))
            }
        else -> error("Unsupported Material 3 layout: $component")
    }
}
