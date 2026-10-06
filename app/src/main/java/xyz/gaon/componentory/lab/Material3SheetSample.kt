package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import kotlinx.coroutines.flow.filterNotNull
import xyz.gaon.componentory.R
import xyz.gaon.componentory.icons.LocalSampleIcon

// The samples drive real sheet state both ways: the button opens through
// state, and drags or dismissals write the genuine state back into the panel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3SheetSample(
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
                rememberStandardBottomSheetState(
                    initialValue =
                        if (state.value == 1) SheetValue.Expanded else SheetValue.PartiallyExpanded,
                    skipHiddenState = true,
                )
            val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
            var sheetReady by remember(state) { mutableStateOf(false) }
            var sheetLaidOut by remember(state) { mutableStateOf(false) }
            LaunchedEffect(state.value, sheetLaidOut) {
                if (!sheetLaidOut) return@LaunchedEffect
                sheetReady = false
                if (state.value == 1) sheetState.expand() else sheetState.partialExpand()
                sheetReady = true
            }
            LaunchedEffect(sheetState) {
                snapshotFlow { if (sheetReady) sheetState.currentValue else null }
                    .filterNotNull()
                    .collect { state.value = if (it == SheetValue.Expanded) 1 else 0 }
            }
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = { Column { sheet() } },
                modifier =
                    modifier.onGloballyPositioned {
                        sheetLaidOut = it.size.width > 0 && it.size.height > 0
                    },
            ) {
                content()
            }
        }
        LabComponent.MODAL_BOTTOM_SHEET -> {
            val sheetState = rememberModalBottomSheetState()
            if (state.value == 1) {
                ModalBottomSheet(
                    onDismissRequest = { state.value = 0 },
                    sheetState = sheetState,
                    modifier = modifier,
                ) {
                    Column { sheet() }
                }
            }
            Column(modifier) { content() }
        }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
