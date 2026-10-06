// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.NavigationRail
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
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
        LabComponent.TAB_ROW,
        LabComponent.SCROLLABLE_TAB_ROW -> Unit
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
    if (component.isTabRow) {
        val tabs = 1..component.tabCount
        val content: @Composable (Int) -> Unit = { tab ->
            if (component == LabComponent.TAB_ROW && tab == 1) {
                LeadingIconTab(
                    selected = state.value == tab,
                    onClick = { state.value = tab },
                    enabled = enabled,
                    icon = { Icon(icon.vector(), contentDescription = null) },
                    text = { Text(stringResource(R.string.list_item, tab)) },
                    modifier = Modifier.testTag("library_${panel}_tab_$tab"),
                )
            } else {
                Tab(
                    selected = state.value == tab,
                    onClick = { state.value = tab },
                    enabled = enabled,
                    text = { Text(stringResource(R.string.list_item, tab)) },
                    modifier = Modifier.testTag("library_${panel}_tab_$tab"),
                )
            }
        }
        if (component == LabComponent.TAB_ROW) {
            TabRow(selectedTabIndex = state.value - 1, modifier = modifier) {
                for (tab in tabs) content(tab)
            }
        } else {
            ScrollableTabRow(selectedTabIndex = state.value - 1, modifier = modifier) {
                for (tab in tabs) content(tab)
            }
        }
    }
}
