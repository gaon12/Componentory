package xyz.gaon.componentory.lab

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

@OptIn(ExperimentalMaterial3Api::class)
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
        LabComponent.TAB_ROW,
        LabComponent.SCROLLABLE_TAB_ROW -> Unit
        LabComponent.TOP_APP_BAR,
        LabComponent.CENTER_ALIGNED_TOP_APP_BAR,
        LabComponent.MEDIUM_TOP_APP_BAR,
        LabComponent.LARGE_TOP_APP_BAR,
        LabComponent.BOTTOM_APP_BAR -> Unit
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
    if (component.isAppBar) {
        val action: @Composable () -> Unit = {
            IconButton(
                onClick = { state.value++ },
                enabled = enabled,
                modifier = Modifier.testTag("library_${panel}_action"),
            ) {
                Icon(icon.vector(), contentDescription = null)
            }
        }
        val title: @Composable () -> Unit = { Text(stringResource(component.labelRes)) }
        when (component) {
            LabComponent.TOP_APP_BAR ->
                TopAppBar(title = title, actions = { action() }, modifier = modifier)
            LabComponent.CENTER_ALIGNED_TOP_APP_BAR ->
                CenterAlignedTopAppBar(title = title, actions = { action() }, modifier = modifier)
            LabComponent.MEDIUM_TOP_APP_BAR ->
                MediumTopAppBar(title = title, actions = { action() }, modifier = modifier)
            LabComponent.LARGE_TOP_APP_BAR ->
                LargeTopAppBar(title = title, actions = { action() }, modifier = modifier)
            LabComponent.BOTTOM_APP_BAR -> BottomAppBar(actions = { action() }, modifier = modifier)
            else -> Unit
        }
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
