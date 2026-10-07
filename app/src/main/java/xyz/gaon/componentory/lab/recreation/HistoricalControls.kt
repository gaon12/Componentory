package xyz.gaon.componentory.lab.recreation

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.Switch
import androidx.annotation.DrawableRes
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.LabComponent
import xyz.gaon.componentory.lab.PlatformFamily

/** Release drawables on current-OS controls, with readable text rather than original captures. */
internal object HistoricalControls {
    private data class Resources(
        @param:DrawableRes val button: Int,
        @param:DrawableRes val checkbox: Int,
        @param:DrawableRes val radio: Int,
        @param:DrawableRes val toggle: Int,
        @param:DrawableRes val input: Int,
        @param:DrawableRes val progress: Int,
        @param:DrawableRes val indeterminate: Int,
        @param:DrawableRes val seekTrack: Int,
        @param:DrawableRes val seekThumb: Int,
        @param:DrawableRes val spinner: Int,
        @param:DrawableRes val switchTrack: Int = 0,
        @param:DrawableRes val switchThumb: Int = 0,
    )

    private val classic =
        Resources(
            R.drawable.aosp_classic_btn_default,
            R.drawable.aosp_classic_btn_check,
            R.drawable.aosp_classic_btn_radio,
            R.drawable.aosp_classic_btn_toggle,
            R.drawable.aosp_classic_edit_text,
            R.drawable.aosp_classic_progress_horizontal,
            R.drawable.aosp_classic_progress_indeterminate_horizontal,
            R.drawable.aosp_classic_progress_horizontal,
            R.drawable.aosp_classic_seek_thumb,
            R.drawable.aosp_classic_spinner_background,
        )
    private val holo =
        Resources(
            R.drawable.aosp_holo_btn_default_holo_light,
            R.drawable.aosp_holo_btn_check_holo_light,
            R.drawable.aosp_holo_btn_radio_holo_light,
            R.drawable.aosp_holo_btn_toggle_holo_light,
            R.drawable.aosp_holo_edit_text_holo_light,
            R.drawable.aosp_holo_progress_horizontal_holo_light,
            R.drawable.aosp_holo_progress_indeterminate_horizontal_holo,
            R.drawable.aosp_holo_scrubber_progress_horizontal_holo_light,
            R.drawable.aosp_holo_scrubber_control_selector_holo,
            R.drawable.aosp_holo_spinner_background_holo_light,
            R.drawable.aosp_holo_switch_track_holo_light,
            R.drawable.aosp_holo_switch_thumb_holo_light,
        )
    private val material =
        Resources(
            R.drawable.aosp_material1_btn_default_material,
            R.drawable.aosp_material1_btn_check_material_anim,
            R.drawable.aosp_material1_btn_radio_material_anim,
            R.drawable.aosp_material1_btn_toggle_material,
            R.drawable.aosp_material1_edit_text_material,
            R.drawable.aosp_material1_progress_horizontal_material,
            R.drawable.aosp_material1_progress_indeterminate_horizontal_material,
            R.drawable.aosp_material1_scrubber_progress_horizontal_material,
            R.drawable.aosp_material1_scrubber_control_selector_material,
            R.drawable.aosp_material1_spinner_background_material,
            R.drawable.aosp_material1_switch_track_material,
            R.drawable.aosp_material1_switch_thumb_material_anim,
        )

    fun supported(family: PlatformFamily, component: LabComponent): Boolean =
        when (component) {
            LabComponent.SWITCH -> family != PlatformFamily.CLASSIC
            LabComponent.BUTTON,
            LabComponent.CHECKBOX,
            LabComponent.RADIO,
            LabComponent.TOGGLE_BUTTON,
            LabComponent.TEXT_FIELD,
            LabComponent.PROGRESS,
            LabComponent.INDETERMINATE_LINEAR_PROGRESS,
            LabComponent.SLIDER,
            LabComponent.SPINNER,
            LabComponent.ANALOG_CLOCK,
            LabComponent.RATING -> true
            else -> false
        }

    fun release(family: PlatformFamily): String =
        when (family) {
            PlatformFamily.CLASSIC -> "android-2.3.7_r1"
            PlatformFamily.HOLO -> "android-4.4.4_r2"
            PlatformFamily.MATERIAL -> "android-5.0.2_r1"
        }

    fun commit(family: PlatformFamily): String =
        when (family) {
            PlatformFamily.CLASSIC -> "3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07"
            PlatformFamily.HOLO -> "63ade05d76785975fc3292ca030abbaa1dda8891"
            PlatformFamily.MATERIAL -> "0ac9664a9ee2e6d1d3b68fe00c264ad94fe98966"
        }

    fun context(base: Context, family: PlatformFamily, component: LabComponent): Context {
        val themed = family.createContext(base)
        return if (family == PlatformFamily.MATERIAL && supported(family, component))
            ContextThemeWrapper(themed, R.style.AospMaterial1Colors)
        else themed
    }

    fun ratingStyle(family: PlatformFamily): Int =
        when (family) {
            PlatformFamily.CLASSIC -> R.style.AospClassicRating
            PlatformFamily.HOLO -> R.style.AospHoloRating
            PlatformFamily.MATERIAL -> R.style.AospMaterial1Rating
        }

    @Suppress("DEPRECATION")
    fun analogClock(base: Context, family: PlatformFamily): android.widget.AnalogClock {
        val layout =
            when (family) {
                PlatformFamily.CLASSIC -> R.layout.aosp_classic_analog_clock
                PlatformFamily.HOLO -> R.layout.aosp_holo_analog_clock
                PlatformFamily.MATERIAL -> R.layout.aosp_material1_analog_clock
            }
        return (LayoutInflater.from(context(base, family, LabComponent.ANALOG_CLOCK))
                .inflate(layout, null) as android.widget.AnalogClock)
            .apply { setTag(R.id.aosp_resource_revision, release(family)) }
    }

    @Suppress("DEPRECATION")
    fun apply(view: View, family: PlatformFamily, component: LabComponent) {
        if (!supported(family, component)) return
        val resources =
            when (family) {
                PlatformFamily.CLASSIC -> classic
                PlatformFamily.HOLO -> holo
                PlatformFamily.MATERIAL -> material
            }
        fun drawable(id: Int) = requireNotNull(view.context.getDrawable(id)).mutate()
        fun background(id: Int) {
            view.backgroundTintList = null
            view.background = drawable(id)
        }
        when (component) {
            LabComponent.BUTTON -> background(resources.button)
            LabComponent.TOGGLE_BUTTON -> background(resources.toggle)
            LabComponent.TEXT_FIELD -> background(resources.input)
            LabComponent.SPINNER -> background(resources.spinner)
            LabComponent.CHECKBOX ->
                (view as CheckBox).apply {
                    buttonTintList = null
                    buttonDrawable = drawable(resources.checkbox)
                }
            LabComponent.RADIO ->
                (view as RadioGroup).apply {
                    for (index in 0 until childCount) (getChildAt(index) as RadioButton).apply {
                        buttonTintList = null
                        buttonDrawable = drawable(resources.radio)
                    }
                }
            LabComponent.SWITCH ->
                (view as Switch).apply {
                    thumbTintList = null
                    trackTintList = null
                    thumbDrawable = drawable(resources.switchThumb)
                    trackDrawable = drawable(resources.switchTrack)
                    showText = family == PlatformFamily.HOLO
                }
            LabComponent.PROGRESS,
            LabComponent.SLIDER,
            LabComponent.RATING,
            LabComponent.INDETERMINATE_LINEAR_PROGRESS ->
                (view as ProgressBar).apply {
                    progressTintList = null
                    progressBackgroundTintList = null
                    secondaryProgressTintList = null
                    indeterminateTintList = null
                    // RatingBar tiles its source stars while parsing the constructor style.
                    if (view !is RatingBar) progressDrawable = drawable(resources.progress)
                    if (component == LabComponent.INDETERMINATE_LINEAR_PROGRESS)
                        indeterminateDrawable = drawable(resources.indeterminate)
                    if (view is SeekBar) {
                        progressDrawable = drawable(resources.seekTrack)
                        view.thumbTintList = null
                        view.thumb = drawable(resources.seekThumb)
                    }
                }
            else -> Unit
        }
        view.setTag(R.id.aosp_resource_revision, release(family))
    }
}
