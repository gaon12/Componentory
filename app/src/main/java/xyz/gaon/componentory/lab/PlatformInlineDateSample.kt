package xyz.gaon.componentory.lab

import android.view.ContextThemeWrapper
import android.widget.CalendarView
import android.widget.DatePicker
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformInlineDateSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val panel = if (viewId == R.id.sample_left) "LEFT" else "RIGHT"
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val calendarWidth = maxOf(maxWidth, 360.dp)
        Box(Modifier.horizontalScroll(rememberScrollState()).testTag("date_viewport_$panel")) {
            when (component) {
                LabComponent.DATE_PICKER ->
                    AndroidView(
                        factory = { context ->
                            val date = SampleDates.parts(requireNotNull(state.inlineDateUtcMillis))
                            DatePicker(ContextThemeWrapper(context, family.themeId)).apply {
                                id = viewId
                                init(date.year, date.month - 1, date.day) { _, year, month, day ->
                                    state.inlineDateUtcMillis =
                                        SampleDates.utcMillis(year, month + 1, day)
                                }
                            }
                        },
                        update = { picker ->
                            picker.isEnabled = enabled
                            val date = SampleDates.parts(requireNotNull(state.inlineDateUtcMillis))
                            if (
                                picker.year != date.year ||
                                    picker.month != date.month - 1 ||
                                    picker.dayOfMonth != date.day
                            ) {
                                picker.updateDate(date.year, date.month - 1, date.day)
                            }
                        },
                        onReset = null,
                        onRelease = { picker ->
                            // init also clears the listener on API 24 and 25.
                            picker.init(picker.year, picker.month, picker.dayOfMonth, null)
                        },
                        modifier = modifier.testTag("native_$panel"),
                    )
                LabComponent.CALENDAR_VIEW ->
                    AndroidView(
                        factory = { context ->
                            CalendarView(ContextThemeWrapper(context, family.themeId)).apply {
                                id = viewId
                                date =
                                    SampleDates.localMillis(
                                        requireNotNull(state.inlineDateUtcMillis)
                                    )
                                setOnDateChangeListener { _, year, month, day ->
                                    state.inlineDateUtcMillis =
                                        SampleDates.utcMillis(year, month + 1, day)
                                }
                            }
                        },
                        update = { calendar ->
                            val selectedDate = requireNotNull(state.inlineDateUtcMillis)
                            if (SampleDates.utcMillisFromLocal(calendar.date) != selectedDate) {
                                calendar.date = SampleDates.localMillis(selectedDate)
                            }
                        },
                        onReset = null,
                        onRelease = { calendar -> calendar.setOnDateChangeListener(null) },
                        modifier =
                            modifier.width(calendarWidth).height(320.dp).testTag("native_$panel"),
                    )
                else -> error("No framework inline date sample for ${component.name}")
            }
        }
    }
}
