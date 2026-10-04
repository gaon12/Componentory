package xyz.gaon.componentory.lab

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
fun PlatformSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier = Modifier,
) {
    // Use framework constructors directly, with no compatibility widget substitution.
    AndroidView(
        factory = { context ->
            createWidget(ContextThemeWrapper(context, family.themeId), component).apply {
                id = viewId
            }
        },
        update = { view -> updateWidget(view, component, enabled, state) },
        onReset = null,
        onRelease = { view ->
            (view.tag as? AlertDialog)?.apply {
                setOnCancelListener(null)
                dismiss()
            }
        },
        modifier = modifier,
    )
}

@Suppress("DEPRECATION")
private fun createWidget(context: Context, component: LabComponent): View =
    when (component) {
        LabComponent.BUTTON -> Button(context).apply { setText(R.string.sample_button) }
        LabComponent.CHECKBOX -> CheckBox(context).apply { setText(R.string.sample_checkbox) }
        LabComponent.SWITCH -> Switch(context).apply { setText(R.string.sample_switch) }
        LabComponent.RADIO ->
            RadioGroup(context).apply {
                addView(
                    RadioButton(context).apply {
                        id = View.generateViewId()
                        setText(R.string.option_a)
                    }
                )
                addView(
                    RadioButton(context).apply {
                        id = View.generateViewId()
                        setText(R.string.option_b)
                    }
                )
            }
        LabComponent.TEXT_FIELD ->
            EditText(context).apply {
                setHint(R.string.sample_hint)
                inputType = InputType.TYPE_CLASS_TEXT
                setSingleLine(true)
            }
        LabComponent.SLIDER -> SeekBar(context).apply { max = 100 }
        LabComponent.PROGRESS ->
            ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
            }
        LabComponent.DIALOG -> Button(context).apply { setText(R.string.open_dialog) }
        LabComponent.TOGGLE_BUTTON ->
            ToggleButton(context).apply {
                textOn = "On"
                textOff = "Off"
            }
        LabComponent.IMAGE_BUTTON ->
            ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_input_add)
                contentDescription = "Add"
                minimumHeight = (48 * resources.displayMetrics.density).toInt()
            }
        LabComponent.RATING ->
            RatingBar(context).apply {
                numStars = 5
                stepSize = 1f
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
            }
        LabComponent.NUMBER_PICKER ->
            NumberPicker(context).apply {
                minValue = 0
                maxValue = 10
                wrapSelectorWheel = false
                descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }

@Suppress("DEPRECATION")
private fun updateWidget(
    view: View,
    component: LabComponent,
    enabled: Boolean,
    state: SampleState,
) {
    view.isEnabled = enabled
    when (component) {
        LabComponent.BUTTON,
        LabComponent.IMAGE_BUTTON -> view.setOnClickListener { state.value++ }
        LabComponent.CHECKBOX,
        LabComponent.SWITCH,
        LabComponent.TOGGLE_BUTTON ->
            (view as CompoundButton).apply {
                setOnCheckedChangeListener(null)
                isChecked = state.value == 1
                setOnCheckedChangeListener { _, checked -> state.value = if (checked) 1 else 0 }
            }
        LabComponent.RADIO ->
            (view as RadioGroup).apply {
                setOnCheckedChangeListener(null)
                for (index in 0 until childCount) getChildAt(index).isEnabled = enabled
                if (state.value == 0) clearCheck() else check(getChildAt(state.value - 1).id)
                setOnCheckedChangeListener { _, checkedId ->
                    state.value =
                        if (checkedId == -1) 0 else if (checkedId == getChildAt(0).id) 1 else 2
                }
            }
        LabComponent.TEXT_FIELD ->
            (view as EditText).apply {
                (tag as? TextWatcher)?.let { removeTextChangedListener(it) }
                if (this.text.toString() != state.text) {
                    setText(state.text)
                    setSelection(state.text.length)
                }
                val watcher =
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) = Unit

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {
                            state.text = s?.toString().orEmpty()
                        }

                        override fun afterTextChanged(s: Editable?) = Unit
                    }
                tag = watcher
                addTextChangedListener(watcher)
            }
        LabComponent.SLIDER ->
            (view as SeekBar).apply {
                progress = state.value
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            if (fromUser) state.value = progress
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    }
                )
            }
        LabComponent.PROGRESS -> (view as ProgressBar).progress = state.value
        LabComponent.RATING ->
            (view as RatingBar).apply {
                setOnRatingBarChangeListener(null)
                rating = state.value.toFloat()
                setOnRatingBarChangeListener { _, rating, fromUser ->
                    if (fromUser) state.value = rating.toInt()
                }
            }
        LabComponent.NUMBER_PICKER ->
            (view as NumberPicker).apply {
                setOnValueChangedListener(null)
                value = state.value
                setOnValueChangedListener { _, _, number -> state.value = number }
            }
        LabComponent.DIALOG ->
            view.setOnClickListener {
                if ((view.tag as? AlertDialog)?.isShowing == true) return@setOnClickListener
                val dialog =
                    AlertDialog.Builder(view.context)
                        .setTitle(R.string.dialog_title)
                        .setMessage(R.string.dialog_message)
                        .setPositiveButton(R.string.dialog_confirm) { _, _ -> state.value = 2 }
                        .setNegativeButton(R.string.dialog_cancel) { _, _ -> state.value = 3 }
                        .setOnCancelListener { state.value = 4 }
                        .create()
                view.tag = dialog
                dialog.show()
                state.value = 1
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}
