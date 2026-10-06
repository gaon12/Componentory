package xyz.gaon.componentory.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

// Swipe state is genuine SwipeToDismissBoxState: drags settle through the real
// threshold logic and confirmValueChange blocks dismissal while disabled.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3DismissSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    if (component != LabComponent.SWIPE_TO_DISMISS)
        error("Unsupported components must be handled by SamplePanel.")
    val dismissState =
        rememberSwipeToDismissBoxState(
            initialValue =
                if (state.value == 1) SwipeToDismissBoxValue.StartToEnd
                else SwipeToDismissBoxValue.Settled,
            confirmValueChange = { enabled },
        )
    LaunchedEffect(state.value) { if (state.value == 0) dismissState.reset() }
    LaunchedEffect(dismissState) {
        snapshotFlow { dismissState.currentValue }
            .collect { state.value = if (it == SwipeToDismissBoxValue.Settled) 0 else 1 }
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(stringResource(R.string.dismiss_background))
            }
        },
        modifier = modifier,
    ) {
        Card(Modifier.fillMaxWidth().testTag("library_${panel}_row")) {
            Text(stringResource(R.string.dismiss_hint), Modifier.padding(16.dp))
        }
    }
}
