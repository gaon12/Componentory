package xyz.gaon.componentory.lab

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.gaon.componentory.R

@Composable
internal fun PlatformTimePickerDialogSample(
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
            if (event == Lifecycle.Event.ON_PAUSE) captureTimeDraft(launcher.value, state)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    AndroidView(
        factory = { context ->
            Button(ContextThemeWrapper(context, family.themeId)).apply {
                id = viewId
                setText(R.string.open_time_picker)
                launcher.value = this
            }
        },
        update = { view ->
            view.isEnabled = enabled
            view.setOnClickListener {
                if ((view.tag as? TimePickerDialog)?.isShowing == true) return@setOnClickListener
                state.timeDraftMinutes = state.timeMinutes
                state.value = 1
                showTimeDialog(view, state)
            }
            if (state.value == 1 && (view.tag as? TimePickerDialog)?.isShowing != true)
                showTimeDialog(view, state)
        },
        onReset = null,
        onRelease = { view ->
            captureTimeDraft(view, state)
            (view.tag as? TimePickerDialog)?.apply {
                setOnCancelListener(null)
                dismiss()
            }
            view.tag = null
            launcher.value = null
        },
        modifier =
            modifier.testTag(if (viewId == R.id.sample_left) "native_LEFT" else "native_RIGHT"),
    )
}

private fun captureTimeDraft(view: Button?, state: SampleState) {
    val dialog = view?.tag as? TimePickerDialog ?: return
    if (state.value != 1 || !dialog.isShowing) return
    val picker = findTimePicker(dialog.window?.decorView) ?: return
    picker.clearFocus()
    state.timeDraftMinutes = SampleTimes.minutes(picker.hour, picker.minute)
}

private fun findTimePicker(view: View?): TimePicker? {
    if (view is TimePicker) return view
    if (view is ViewGroup) {
        for (index in 0 until view.childCount) {
            findTimePicker(view.getChildAt(index))?.let {
                return it
            }
        }
    }
    return null
}

private fun showTimeDialog(view: Button, state: SampleState) {
    val initial = SampleTimes.parts(state.timeDraftMinutes ?: state.timeMinutes)
    val dialog =
        TimePickerDialog(
            view.context,
            { _, hour, minute ->
                state.timeMinutes = SampleTimes.minutes(hour, minute)
                state.timeDraftMinutes = null
                state.value = 2
            },
            initial.hour,
            initial.minute,
            state.time24Hour,
        )
    dialog.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        view.context.getText(android.R.string.cancel),
    ) { window, which ->
        state.value = 3
        dialog.onClick(window, which)
    }
    dialog.setOnCancelListener {
        if (state.value != 3) state.value = 4
        state.timeDraftMinutes = null
    }
    view.tag = dialog
    // Keep show()'s original positive listener, including input validation and focus handling.
    dialog.show()
}
