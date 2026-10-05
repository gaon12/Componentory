package xyz.gaon.componentory.lab

import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformChronometerSample(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    AndroidView(
        factory = { context ->
            val themed = ContextThemeWrapper(context, family.themeId)
            val chronometer = Chronometer(themed).apply { id = viewId }
            val controls =
                LinearLayout(themed).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(
                        chronometerAction(
                            themed,
                            "chronometer_start_$panel",
                            R.string.chronometer_start,
                        ) {
                            state.chronometerBaseMillis =
                                System.currentTimeMillis() - elapsed(state)
                            state.value = 1
                        }
                    )
                    addView(
                        chronometerAction(
                            themed,
                            "chronometer_stop_$panel",
                            R.string.chronometer_stop,
                        ) {
                            state.chronometerBaseMillis = elapsed(state)
                            state.value = 0
                        }
                    )
                    addView(
                        chronometerAction(
                            themed,
                            "chronometer_reset_$panel",
                            R.string.chronometer_reset,
                        ) {
                            state.chronometerBaseMillis =
                                if (state.value == 1) System.currentTimeMillis() else 0
                        }
                    )
                }
            LinearLayout(themed).apply {
                orientation = LinearLayout.VERTICAL
                addView(chronometer)
                addView(controls)
            }
        },
        update = { root ->
            val holder = (root.tag as? ChronometerSync) ?: ChronometerSync().also { root.tag = it }
            val chronometer = requireNotNull(root.findViewById<Chronometer>(viewId))
            val controls = root.getChildAt(1) as LinearLayout
            val running = state.value == 1
            if (holder.anchor != state.chronometerBaseMillis || holder.running != running) {
                chronometer.base = SystemClock.elapsedRealtime() - elapsed(state)
                if (running) chronometer.start() else chronometer.stop()
                holder.anchor = state.chronometerBaseMillis
                holder.running = running
            }
            chronometer.isEnabled = enabled
            controls.getChildAt(0).isEnabled = enabled && !running
            controls.getChildAt(1).isEnabled = enabled && running
            controls.getChildAt(2).isEnabled = enabled
        },
        onRelease = { root -> requireNotNull(root.findViewById<Chronometer>(viewId)).stop() },
        modifier = modifier.testTag("native_$panel"),
    )
}

private class ChronometerSync(var anchor: Long = 0, var running: Boolean = false)

private fun chronometerAction(
    context: android.content.Context,
    tag: String,
    textRes: Int,
    action: () -> Unit,
) =
    Button(context).apply {
        setText(textRes)
        this.tag = tag
        contentDescription = context.getString(textRes)
        setOnClickListener { action() }
        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).let {
            layoutParams = it
        }
    }

private fun elapsed(state: SampleState): Long {
    val base = state.chronometerBaseMillis
    return if (state.value == 1) {
        if (base > 0) (System.currentTimeMillis() - base).coerceAtLeast(0) else 0
    } else base.coerceAtLeast(0)
}
