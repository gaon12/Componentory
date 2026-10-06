package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

// The samples drive a real DrawerState both ways: the button opens through
// state, and scrim dismissals write the genuine state back into the panel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3DrawerSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val icon = requireNotNull(LocalSampleIcon.current)
    val openButton: @Composable () -> Unit = {
        Button(
            onClick = { state.value = 1 },
            enabled = enabled,
            modifier = Modifier.testTag("library_${panel}_open"),
        ) {
            Text(stringResource(R.string.open_drawer))
        }
    }
    val items: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
        for (item in 1..3) {
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.drawer_item, item)) },
                selected = false,
                onClick = { state.value = 0 },
                icon = { Icon(icon.vector(), contentDescription = null) },
                modifier = Modifier.testTag("library_${panel}_item_$item"),
            )
        }
    }
    when (component) {
        LabComponent.MODAL_NAVIGATION_DRAWER -> {
            val drawerState =
                rememberDrawerState(if (state.value == 1) DrawerValue.Open else DrawerValue.Closed)
            LaunchedEffect(state.value) {
                if (state.value == 1) drawerState.open() else drawerState.close()
            }
            LaunchedEffect(drawerState) {
                snapshotFlow { drawerState.currentValue }
                    .collect { state.value = if (it == DrawerValue.Open) 1 else 0 }
            }
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = enabled,
                drawerContent = { ModalDrawerSheet { Column { items() } } },
                modifier = modifier,
            ) {
                openButton()
            }
        }
        LabComponent.DISMISSIBLE_NAVIGATION_DRAWER -> {
            val drawerState =
                rememberDrawerState(if (state.value == 1) DrawerValue.Open else DrawerValue.Closed)
            LaunchedEffect(state.value) {
                if (state.value == 1) drawerState.open() else drawerState.close()
            }
            LaunchedEffect(drawerState) {
                snapshotFlow { drawerState.currentValue }
                    .collect { state.value = if (it == DrawerValue.Open) 1 else 0 }
            }
            DismissibleNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = enabled,
                drawerContent = { DismissibleDrawerSheet { Column { items() } } },
                modifier = modifier,
            ) {
                openButton()
            }
        }
        LabComponent.PERMANENT_NAVIGATION_DRAWER ->
            PermanentNavigationDrawer(
                drawerContent = { PermanentDrawerSheet { Column { items() } } },
                modifier = modifier,
            ) {
                openButton()
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
