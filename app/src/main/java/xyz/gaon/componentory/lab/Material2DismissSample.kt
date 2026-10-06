// Both libraries remain separate sample families, including their experimental public controls.
@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.gaon.componentory.R

// Swipe state is genuine DismissState: drags settle through the real
// threshold logic and confirmStateChange blocks dismissal while disabled.
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Material2DismissSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    if (component != LabComponent.SWIPE_TO_DISMISS)
        error("Unsupported components must be handled by SamplePanel.")
    val dismissState =
        rememberDismissState(
            initialValue =
                if (state.value == 1) DismissValue.DismissedToEnd else DismissValue.Default,
            confirmStateChange = { enabled },
        )
    LaunchedEffect(state.value) { if (state.value == 0) dismissState.reset() }
    LaunchedEffect(dismissState) {
        snapshotFlow { dismissState.currentValue }
            .collect { state.value = if (it == DismissValue.Default) 0 else 1 }
    }
    SwipeToDismiss(
        state = dismissState,
        background = {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colors.secondary).padding(8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(stringResource(R.string.dismiss_background))
            }
        },
        dismissContent = {
            Card(Modifier.fillMaxWidth().testTag("library_${panel}_row")) {
                Text(stringResource(R.string.dismiss_hint), Modifier.padding(16.dp))
            }
        },
        modifier = modifier,
    )
}
