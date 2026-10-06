// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.Button
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

// The samples drive real drawer state both ways: the button opens through
// state, and scrim dismissals write the genuine state back into the panel.
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2DrawerSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val icon = requireNotNull(LocalSampleIcon.current)
    val content: @Composable () -> Unit = {
        Button(
            onClick = { state.value = 1 },
            enabled = enabled,
            modifier = Modifier.testTag("library_${panel}_open"),
        ) {
            Text(stringResource(R.string.open_drawer))
        }
    }
    val sheet: @Composable () -> Unit = {
        for (item in 1..3) {
            TextButton(
                onClick = { state.value = 0 },
                modifier = Modifier.testTag("library_${panel}_item_$item"),
            ) {
                Icon(icon.vector(), contentDescription = null)
                Text(stringResource(R.string.drawer_item, item))
            }
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
            ModalDrawer(
                drawerState = drawerState,
                gesturesEnabled = enabled,
                drawerContent = { Column(Modifier.fillMaxWidth(0.7f)) { sheet() } },
                modifier = modifier,
            ) {
                content()
            }
        }
        LabComponent.BOTTOM_DRAWER -> {
            val drawerState =
                rememberBottomDrawerState(
                    if (state.value == 1) BottomDrawerValue.Expanded else BottomDrawerValue.Closed
                )
            LaunchedEffect(state.value) {
                if (state.value == 1) drawerState.expand() else drawerState.close()
            }
            LaunchedEffect(drawerState) {
                snapshotFlow { drawerState.currentValue }
                    .collect { state.value = if (it == BottomDrawerValue.Expanded) 1 else 0 }
            }
            BottomDrawer(
                drawerState = drawerState,
                gesturesEnabled = enabled,
                drawerContent = { sheet() },
                modifier = modifier,
            ) {
                content()
            }
        }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
