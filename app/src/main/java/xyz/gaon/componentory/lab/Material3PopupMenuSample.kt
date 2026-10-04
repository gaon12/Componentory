package xyz.gaon.componentory.lab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.gaon.componentory.R

@Composable
internal fun Material3PopupMenuSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    var expanded by remember(panel, state) { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, state) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) expanded = false
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(enabled) { if (!enabled) expanded = false }
    // The inner Box measures only the launcher, so the menu has a real compact anchor.
    Box(Modifier.fillMaxWidth()) {
        Box {
            Button(
                onClick = {
                    state.text = SampleMenuAction.OPENED.name
                    expanded = true
                },
                enabled = enabled,
                modifier = modifier,
            ) {
                Text(stringResource(R.string.open_menu))
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = {
                    if (expanded && enabled && state.text == SampleMenuAction.OPENED.name)
                        state.text = SampleMenuAction.DISMISSED.name
                    expanded = false
                },
                modifier = Modifier.testTag("menu_$panel"),
            ) {
                listOf(
                        "A" to R.string.option_a,
                        "B" to R.string.option_b,
                        "DISABLED" to R.string.menu_disabled_choice,
                    )
                    .forEachIndexed { index, (tag, label) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label)) },
                            onClick = {
                                state.value = index + 1
                                state.text = SampleMenuAction.SELECTED.name
                                expanded = false
                            },
                            enabled = enabled && index < 2,
                            modifier = Modifier.testTag("menu_item_${panel}_$tag"),
                        )
                    }
            }
        }
    }
}
