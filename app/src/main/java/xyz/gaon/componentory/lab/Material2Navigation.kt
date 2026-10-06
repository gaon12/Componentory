// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@Composable
internal fun Material2Navigation(
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
            BottomNavigation(modifier = modifier) {
                for (item in items) {
                    BottomNavigationItem(
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
