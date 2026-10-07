package xyz.gaon.componentory.lab

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.widget.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.gaon.componentory.R

@Composable
internal fun PlatformDatePickerDialogSample(
    family: PlatformFamily,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    val launcher = remember { mutableStateOf<Button?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, state) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) captureDraft(launcher.value, state)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    ReadableAndroidView(
        factory = { context ->
            Button(family.createContext(context)).apply {
                id = viewId
                setText(R.string.open_date_picker)
                launcher.value = this
            }
        },
        update = { view ->
            view.isEnabled = enabled
            view.setOnClickListener {
                if ((view.tag as? DatePickerDialog)?.isShowing == true) return@setOnClickListener
                state.dateDraftUtcMillis = state.dateUtcMillis
                state.value = 1
                showDialog(view, state)
            }
            if (state.value == 1 && (view.tag as? DatePickerDialog)?.isShowing != true)
                showDialog(view, state)
        },
        onReset = null,
        onRelease = { view ->
            captureDraft(view, state)
            (view.tag as? DatePickerDialog)?.apply {
                setOnCancelListener(null)
                setOnDateSetListener(null)
                dismiss()
            }
            view.tag = null
            launcher.value = null
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

private fun captureDraft(view: Button?, state: SampleState) {
    val dialog = view?.tag as? DatePickerDialog ?: return
    if (state.value != 1 || !dialog.isShowing) return
    val picker = dialog.datePicker
    picker.clearFocus()
    state.dateDraftUtcMillis =
        SampleDates.utcMillis(picker.year, picker.month + 1, picker.dayOfMonth)
}

private fun showDialog(view: Button, state: SampleState) {
    val initial = SampleDates.parts(state.dateDraftUtcMillis ?: state.dateUtcMillis)
    val dialog =
        DatePickerDialog(
            view.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                state.dateUtcMillis =
                    SampleDates.utcMillis(selectedYear, selectedMonth + 1, selectedDay)
                state.dateDraftUtcMillis = null
                state.value = 2
            },
            initial.year,
            initial.month - 1,
            initial.day,
        )
    dialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        view.context.getText(android.R.string.cancel),
    ) { window, which ->
        state.value = 3
        // Forward the original cancellation path; positive focus and validation stay untouched.
        dialog.onClick(window, which)
    }
    dialog.setOnCancelListener {
        if (state.value != 3) state.value = 4
        state.dateDraftUtcMillis = null
    }
    view.tag = dialog
    dialog.show()
}
