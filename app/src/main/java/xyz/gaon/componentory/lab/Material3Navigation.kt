package xyz.gaon.componentory.lab

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
internal fun Material3Navigation(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val icon = requireNotNull(LocalSampleIcon.current)
    val items = 1..component.navigationItemCount
    when (component) {
        LabComponent.NAVIGATION_BAR ->
            NavigationBar(modifier = modifier) {
                for (item in items) {
                    NavigationBarItem(
                        selected = state.value == item,
                        onClick = { state.value = item },
                        enabled = enabled,
                        icon = { Icon(icon.vector(), contentDescription = null) },
                        label = { Text(stringResource(R.string.list_item, item)) },
                        modifier = Modifier.testTag("library_${panel}_item_$item"),
                    )
                }
            }
        LabComponent.NAVIGATION_RAIL ->
            NavigationRail(modifier = modifier) {
                for (item in items) {
                    NavigationRailItem(
                        selected = state.value == item,
                        onClick = { state.value = item },
                        enabled = enabled,
                        icon = { Icon(icon.vector(), contentDescription = null) },
                        label = { Text(stringResource(R.string.list_item, item)) },
                        modifier = Modifier.testTag("library_${panel}_item_$item"),
                    )
                }
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
