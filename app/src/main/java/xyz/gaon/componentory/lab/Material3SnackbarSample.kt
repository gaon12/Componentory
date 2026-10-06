package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import xyz.gaon.componentory.R

// A Snackbar only exists while a real SnackbarHost shows it; the sample is the
// button plus the host so every tap drives the genuine queue and surface.
@Composable
internal fun Material3SnackbarSample(
    component: LabComponent,
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    if (component != LabComponent.SNACKBAR)
        error("Unsupported components must be handled by SamplePanel.")
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.snackbar_message)
    Box(modifier) {
        Button(
            onClick = {
                state.value++
                scope.launch { hostState.showSnackbar(message) }
            },
            enabled = enabled,
            modifier = Modifier.testTag("library_${panel}_show"),
        ) {
            Text(stringResource(R.string.show_snackbar))
        }
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier.align(Alignment.BottomCenter).testTag("library_${panel}_host"),
        )
    }
}
