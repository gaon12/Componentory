// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filterNotNull
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

// The samples drive real sheet state both ways: the button opens through
// state, and drags or dismissals write the genuine state back into the panel.
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2SheetSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val icon = requireNotNull(LocalSampleIcon.current)
    // Restored anchors can report a temporary closed value before the requested state is applied.
    var sheetReady by remember(state) { mutableStateOf(false) }
    var sheetLaidOut by remember(state) { mutableStateOf(false) }
    val sheetHost =
        modifier.onGloballyPositioned { sheetLaidOut = it.size.width > 0 && it.size.height > 0 }
    val content: @Composable () -> Unit = {
        Button(
            onClick = { state.value = 1 },
            enabled = enabled,
            modifier = Modifier.testTag("library_${panel}_open"),
        ) {
            Text(stringResource(R.string.open_sheet))
        }
    }
    val sheet: @Composable () -> Unit = {
        for (item in 1..3) {
            TextButton(
                onClick = { state.value = 0 },
                modifier = Modifier.testTag("library_${panel}_item_$item"),
            ) {
                Icon(icon.vector(), contentDescription = null)
                Text(stringResource(R.string.list_item, item))
            }
        }
    }
    when (component) {
        LabComponent.BOTTOM_SHEET_SCAFFOLD -> {
            val sheetState =
                rememberBottomSheetState(
                    if (state.value == 1) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
                )
            val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
            LaunchedEffect(state.value, sheetLaidOut) {
                if (!sheetLaidOut) return@LaunchedEffect
                sheetReady = false
                if (state.value == 1) sheetState.expand() else sheetState.collapse()
                sheetReady = true
            }
            LaunchedEffect(sheetState) {
                snapshotFlow { if (sheetReady) sheetState.currentValue else null }
                    .filterNotNull()
                    .collect { state.value = if (it == BottomSheetValue.Expanded) 1 else 0 }
            }
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetGesturesEnabled = enabled,
                sheetContent = { Column { sheet() } },
                modifier = sheetHost,
            ) {
                content()
            }
        }
        LabComponent.MODAL_BOTTOM_SHEET -> {
            val sheetState =
                rememberModalBottomSheetState(
                    if (state.value == 1) ModalBottomSheetValue.Expanded
                    else ModalBottomSheetValue.Hidden
                )
            LaunchedEffect(state.value, sheetLaidOut) {
                if (!sheetLaidOut) return@LaunchedEffect
                sheetReady = false
                if (state.value == 1) sheetState.show() else sheetState.hide()
                sheetReady = true
            }
            LaunchedEffect(sheetState) {
                snapshotFlow { if (sheetReady) sheetState.currentValue else null }
                    .filterNotNull()
                    .collect { state.value = if (it == ModalBottomSheetValue.Hidden) 0 else 1 }
            }
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetGesturesEnabled = enabled,
                sheetContent = { Column { sheet() } },
                modifier = sheetHost,
            ) {
                content()
            }
        }
        LabComponent.BACKDROP_SCAFFOLD -> {
            val backdropState =
                rememberBackdropScaffoldState(
                    if (state.value == 1) BackdropValue.Revealed else BackdropValue.Concealed
                )
            LaunchedEffect(state.value, sheetLaidOut) {
                if (!sheetLaidOut) return@LaunchedEffect
                sheetReady = false
                if (state.value == 1) backdropState.reveal() else backdropState.conceal()
                sheetReady = true
            }
            LaunchedEffect(backdropState) {
                snapshotFlow { if (sheetReady) backdropState.currentValue else null }
                    .filterNotNull()
                    .collect { state.value = if (it == BackdropValue.Revealed) 1 else 0 }
            }
            BackdropScaffold(
                scaffoldState = backdropState,
                gesturesEnabled = enabled,
                appBar = {},
                backLayerContent = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.sheet_back_layer),
                            Modifier.padding(8.dp).testTag("library_${panel}_back"),
                        )
                        sheet()
                    }
                },
                frontLayerContent = { Box(Modifier.padding(8.dp)) { content() } },
                modifier = sheetHost,
            )
        }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
