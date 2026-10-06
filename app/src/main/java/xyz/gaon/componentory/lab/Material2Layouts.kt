// The lab keeps samples from different Material libraries separate.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2Layouts(component: LabComponent, modifier: Modifier) {
    when (component) {
        LabComponent.HORIZONTAL_DIVIDER -> Divider(modifier = modifier)
        LabComponent.LIST_ITEM ->
            ListItem(
                text = { Text(stringResource(R.string.list_item, 1)) },
                secondaryText = { Text(stringResource(R.string.component_list_item)) },
                modifier = modifier,
            )
        LabComponent.SCAFFOLD ->
            Scaffold(modifier = modifier) { padding ->
                Text(stringResource(R.string.scaffold_body), modifier = Modifier.padding(padding))
            }
        else -> error("Unsupported Material 2 layout: $component")
    }
}
