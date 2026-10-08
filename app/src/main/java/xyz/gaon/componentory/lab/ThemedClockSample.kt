@file:Suppress("UsingMaterialAndMaterial3Libraries")

package xyz.gaon.componentory.lab

import android.os.SystemClock
import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.Text as Material2Text
import androidx.compose.material.TextButton as Material2TextButton
import androidx.compose.material.Typography as Material2Typography
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import xyz.gaon.componentory.R

internal fun DesignFamily.usesThemedClock(component: LabComponent): Boolean =
    platform == null && (component.isClockDisplay || component == LabComponent.CHRONOMETER)

/** A Componentory demo using library themes, not a claimed Material clock API. */
@Composable
internal fun ThemedClockSample(
    family: DesignFamily,
    component: LabComponent,
    panel: String,
    enabled: Boolean,
    state: SampleState,
) {
    if (family == DesignFamily.MATERIAL2)
        Material2Theme(colors = lightColors(), typography = Material2Typography()) {
            ClockContent(family, component, panel, enabled, state)
        }
    else Material3SampleTheme(family) { ClockContent(family, component, panel, enabled, state) }
}

@Composable
private fun ClockContent(
    family: DesignFamily,
    component: LabComponent,
    panel: String,
    enabled: Boolean,
    state: SampleState,
) {
    val now =
        produceState(System.currentTimeMillis(), component, state.value) {
                value = System.currentTimeMillis()
                if (component == LabComponent.CHRONOMETER && state.value != 1) return@produceState
                while (true) {
                    delay(1000L - System.currentTimeMillis() % 1000L)
                    value = System.currentTimeMillis()
                }
            }
            .value
    val locale = LocalConfiguration.current.locales[0]
    val use24Hour =
        if (component == LabComponent.TEXT_CLOCK) state.time24Hour
        else DateFormat.is24HourFormat(LocalContext.current)
    val formatter =
        remember(locale, use24Hour) {
            SimpleDateFormat(if (use24Hour) "HH:mm:ss" else "h:mm:ss a", locale)
        }
    // Restore the saved wall-clock base once, then time a running session monotonically.
    val runningBase =
        remember(state.value, state.chronometerBaseMillis) {
            val restoredElapsed =
                if (state.value == 1 && state.chronometerBaseMillis > 0)
                    (System.currentTimeMillis() - state.chronometerBaseMillis).coerceAtLeast(0)
                else 0
            SystemClock.elapsedRealtime() - restoredElapsed
        }
    val elapsed =
        if (state.value == 1) {
            (SystemClock.elapsedRealtime() - runningBase).coerceAtLeast(0)
        } else state.chronometerBaseMillis.coerceAtLeast(0)
    val text =
        if (component == LabComponent.CHRONOMETER) SampleTimes.formatElapsed(elapsed)
        else formatter.apply { timeZone = TimeZone.getDefault() }.format(Date(now))
    val material2 = family == DesignFamily.MATERIAL2
    val foreground =
        if (material2) Material2Theme.colors.onSurface else MaterialTheme.colorScheme.onSurface
    val primary =
        if (material2) Material2Theme.colors.primary else MaterialTheme.colorScheme.primary
    val face =
        if (material2) primary.copy(alpha = .12f) else MaterialTheme.colorScheme.primaryContainer
    val markings = if (material2) foreground else MaterialTheme.colorScheme.onPrimaryContainer
    val typography =
        if (material2) Material2Theme.typography.h4 else MaterialTheme.typography.displaySmall
    Column(
        Modifier.testTag("themed_clock_$panel"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (component == LabComponent.ANALOG_CLOCK) {
            val time = Calendar.getInstance(locale).apply { timeInMillis = now }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                Canvas(
                    Modifier.size(maxWidth.coerceAtMost(224.dp))
                        .testTag("clock_dial_$panel")
                        .semantics { contentDescription = text }
                ) {
                    val radius = size.minDimension / 2
                    drawCircle(face, radius)
                    fun point(units: Float, cycle: Float, length: Float): Offset {
                        val angle = units / cycle * (2 * Math.PI) - Math.PI / 2
                        return center + Offset(cos(angle).toFloat(), sin(angle).toFloat()) * length
                    }
                    repeat(12) { mark ->
                        drawLine(
                            markings,
                            point(mark.toFloat(), 12f, radius * .82f),
                            point(mark.toFloat(), 12f, radius * .9f),
                            2.dp.toPx(),
                            StrokeCap.Round,
                        )
                    }
                    val minute = time.get(Calendar.MINUTE) + time.get(Calendar.SECOND) / 60f
                    val hour = time.get(Calendar.HOUR) + minute / 60f
                    drawLine(
                        primary,
                        center,
                        point(hour, 12f, radius * .48f),
                        6.dp.toPx(),
                        StrokeCap.Round,
                    )
                    drawLine(
                        primary,
                        center,
                        point(minute, 60f, radius * .7f),
                        4.dp.toPx(),
                        StrokeCap.Round,
                    )
                    drawCircle(primary, 5.dp.toPx())
                }
            }
        } else
            BasicText(
                text,
                modifier = Modifier.fillMaxWidth().testTag("clock_text_$panel"),
                style = typography.copy(color = foreground, fontFamily = FontFamily.Monospace),
                maxLines = 2,
                autoSize =
                    TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = typography.fontSize),
            )
        if (component == LabComponent.CHRONOMETER)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ClockAction(
                    family,
                    R.string.chronometer_start,
                    "chronometer_start_$panel",
                    enabled && state.value != 1,
                ) {
                    state.chronometerBaseMillis = System.currentTimeMillis() - elapsed
                    state.value = 1
                }
                ClockAction(
                    family,
                    R.string.chronometer_stop,
                    "chronometer_stop_$panel",
                    enabled && state.value == 1,
                ) {
                    state.chronometerBaseMillis =
                        (SystemClock.elapsedRealtime() - runningBase).coerceAtLeast(0)
                    state.value = 0
                }
                ClockAction(
                    family,
                    R.string.chronometer_reset,
                    "chronometer_reset_$panel",
                    enabled,
                ) {
                    state.chronometerBaseMillis =
                        if (state.value == 1) System.currentTimeMillis() else 0
                }
            }
    }
}

@Composable
private fun ClockAction(
    family: DesignFamily,
    text: Int,
    tag: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (family == DesignFamily.MATERIAL2)
        Material2TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.testTag(tag),
        ) {
            Material2Text(stringResource(text), fontSize = 16.sp)
        }
    else
        TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.testTag(tag)) {
            Text(stringResource(text), fontSize = 16.sp)
        }
}
