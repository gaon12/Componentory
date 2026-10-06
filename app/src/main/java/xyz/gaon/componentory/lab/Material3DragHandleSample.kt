package xyz.gaon.componentory.lab

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

// The handle resizes a real pane: dragging it writes the pane width percent
// into the sample state, which is the copied input.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Material3DragHandleSample(
    panel: String,
    modifier: Modifier,
    enabled: Boolean,
    state: SampleState,
) {
    val density = LocalDensity.current
    var hostSize by remember { mutableStateOf(IntSize.Zero) }
    Box(modifier.fillMaxWidth().onGloballyPositioned { hostSize = it.size }) {
        val paneWidth =
            with(density) { (hostSize.width * state.value / 100).toDp().coerceAtLeast(48.dp) }
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 0.dp),
            modifier = Modifier.width(paneWidth).fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                VerticalDragHandle(
                    modifier =
                        Modifier.testTag("library_${panel}_handle")
                            .draggable(
                                orientation = Orientation.Horizontal,
                                enabled = enabled,
                                state =
                                    rememberDraggableState { delta ->
                                        if (hostSize.width > 0) {
                                            state.value =
                                                (state.value + delta * 100 / hostSize.width)
                                                    .toInt()
                                                    .coerceIn(0, 100)
                                        }
                                    },
                            )
                            .systemGestureExclusion()
                )
            }
        }
    }
}
